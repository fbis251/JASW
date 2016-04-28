package com.fernandobarillas.redditservice.data;

import android.os.AsyncTask;
import android.util.Log;

import com.fernandobarillas.redditservice.callbacks.LinkDownloadCallback;
import com.fernandobarillas.redditservice.callbacks.RedditLinksCallback;
import com.fernandobarillas.redditservice.exceptions.LinksListNullException;
import com.fernandobarillas.redditservice.links.filters.LinkFilter;
import com.fernandobarillas.redditservice.links.validators.LinkValidator;
import com.fernandobarillas.redditservice.models.Link;
import com.fernandobarillas.redditservice.requests.LinkDownloadRequest;
import com.fernandobarillas.redditservice.requests.SubredditRequest;
import com.fernandobarillas.redditservice.tasks.LinkDownloadTask;

import net.dean.jraw.RedditClient;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * A class to easily download Links using the reddit API. The Links are verified for validity and
 * all duplicates are removed. Downloads happen in a background thread to prevent blocking. There is
 * also support for downloading posts from specific subreddits and sorting the Links using reddit's
 * API (Hot, New, Top, etc.), and filtering out any posts marked as NSFW.
 */
public class RedditLinks {
    public static final int MAX_LINK_LIMIT = 100; // Max link limit supported by reddit API
    // Last Link viewed tracking
    public static final int NO_LAST_LINK_VIEWED = -1;
    private static final String LOG_TAG = "RedditLinks";
    // Reddit client and data
    private ArrayList<Link> mLinksList;

    private RedditClient mRedditClient;

    private SubredditPaginator mSubredditPaginator;

    private SubredditRequest mSubredditRequest;

    private LinkDownloadTask mLinkDownloadTask;

    private int mLastViewedLink = NO_LAST_LINK_VIEWED;
    // Used to keep position, for example in a RecyclerView

    private int mNsfwImageCount; // How many nsfw images were downloaded

    public RedditLinks(RedditClient redditClient) {
        Log.d(LOG_TAG, "RedditLinks() called with: " + "redditClient = [" + redditClient + "]");
        mRedditClient = redditClient;
        mLinksList = new ArrayList<>();
        mSubredditPaginator = null;
        mSubredditRequest = null;
        mNsfwImageCount = 0;
    }

    /**
     * This method initiates the AsyncTask that downloads new reddit Links. This method is
     * synchronized because we don't want to have more than one download task executing at a time.
     * For example, this helps prevent excessive downloads from happening as a user swipes through
     * the gallery.
     */
    private synchronized void executeLinkDownload(final SubredditRequest subredditRequest) {
        Log.d(LOG_TAG, "executeLinkDownload() called with: " + "subredditRequest = [" + subredditRequest + "]");

        validatePaginator(subredditRequest);

        LinkDownloadRequest linkDownloadRequest =
                new LinkDownloadRequest(mRedditClient, mSubredditPaginator, linkDownloadCallbackHandler(subredditRequest));
        // Create a new download task to get more Links from reddit
        mLinkDownloadTask = new LinkDownloadTask();
        mLinkDownloadTask.execute(linkDownloadRequest);
    }

    /**
     * Gets the number of Links the app has downloaded from Reddit.
     *
     * @return The total count of image Links the app has downloaded.
     */
    public int getCount() {
        return mLinksList.size();
    }

    /**
     * Returns the last viewed Link. Needs to be set manually
     */
    public int getLastViewedLink() {
        return mLastViewedLink;
    }

    /**
     * Sets the last viewed Link in the Link List
     */
    public void setLastViewedLink(int lastViewedLink) {
        mLastViewedLink = lastViewedLink;
    }

    /**
     * Gets a specific reddit Link from the List.
     *
     * @param whichLink The ID of the Link to return.
     * @return The Link with ID whichLink.
     */
    public Link getLink(int whichLink) {
        if (whichLink < mLinksList.size() && whichLink >= 0) {
            return mLinksList.get(whichLink);
        }

        return null;
    }

    /**
     * This method verifies that we are not currently attempting to download reddit links. Because
     * executeLinkDownload is a synchronized method, we do not want to queue more calls to it than
     * necessary.
     */
    public void getMoreLinks(final RedditLinksCallback linksCallback) {
        Log.v(LOG_TAG, "getMoreLinks()");

        if (mLinkDownloadTask != null && mLinkDownloadTask.getStatus() == AsyncTask.Status.RUNNING) {
            Log.w(LOG_TAG, "DownloadFilesTask Already downloading data, returning");
            return;
        }

        mSubredditRequest.setRedditLinksCallback(linksCallback);
        executeLinkDownload(mSubredditRequest);
    }

    /**
     * Notifies the LinkDownloadTask to get new links instead of getting more links from reddit.
     *
     * @param subredditRequest The new subreddit to download links from
     */
    public void getNewLinks(final SubredditRequest subredditRequest) {
        Log.v(LOG_TAG, "getNewLinks() called with: " + "subredditRequest = [" + subredditRequest + "]");
        // Discard the old List to force a download of new data
        mLinksList = new ArrayList<>();
        // Force instantiation of a new Paginator to avoid it throwing an IllegalStateException
        mSubredditPaginator = null;
        // Use the passed in subredditRequest to pass into our download task and paginators
        mSubredditRequest = subredditRequest;

        // Reset the last visible link
        mLastViewedLink = 0;

        // Cancel the current download task since we will be downloading new links
        if (mLinkDownloadTask != null) {
            Log.i(LOG_TAG, "Cancelling pending link download");
            mLinkDownloadTask.cancel(true);
            mLinkDownloadTask = null;
        }

        getMoreLinks(subredditRequest.getRedditLinksCallback());
    }

    /**
     * Gets the number of NSFW images that were downloaded in the last request. Useful when no
     * "valid" links were downloaded and the user has NSFW images disabled. This will allow the user
     * to be displayed a message asking them to enable NSFW content if desired
     *
     * @return The number of NSFW images that were downloaded in the last request.
     */
    public int getNsfwImageCount() {
        return mNsfwImageCount;
    }

    /**
     * Gets the current, full List of reddit Links the application has downloaded.
     *
     * @return A List containing all the downloaded reddit Links.
     */
    public List<Link> getRedditLinksList() {
        Log.v(LOG_TAG, "getRedditLinks()");

        return mLinksList;
    }

    /**
     * Handles adding newly downloaded Links to the main LinksList. This method performs validation
     * and filtering based on the LinkValidator and LinkFilter instances passed in via the
     * SubredditRequest
     *
     * @param subredditRequest The request which contains instances of {@link LinkValidator} and
     *                         {@link LinkFilter} instances
     * @return The onComplete instance to be used after new links are successfully downloaded
     */
    private LinkDownloadCallback linkDownloadCallbackHandler(final SubredditRequest subredditRequest) {
        Log.d(LOG_TAG, "linkDownloadCallbackHandler() called with: " + "subredditRequest = [" + subredditRequest + "]");
        return new LinkDownloadCallback() {
            @Override
            public void linkDownloadCallback(List<Link> newLinksList, Exception e) {
                Log.v(LOG_TAG, "linkDownloadCallback() called with: " + "newLinksList = [" + newLinksList + "], e = [" + e + "]");
                RedditLinksCallback redditLinksCallback = subredditRequest.getRedditLinksCallback();
                if (newLinksList == null) {
                    Log.e(LOG_TAG, "linkDownloadCallback: Links list was null! Unknown error");
                    if (redditLinksCallback != null) {
                        redditLinksCallback.linksCallback(new LinksListNullException());
                    }

                    return;
                }

                // Attempt to only add Links that are marked as valid by the passed in LinkValidator
                LinkValidator linkValidator = subredditRequest.getLinkValidator();
                mNsfwImageCount = 0;
                int oldLinkCount = mLinksList.size();
                if (linkValidator != null) {
                    Log.d(LOG_TAG, "linkDownloadCallback: Validating Links");
                    // Only add valid Links to the List
                    for (Link link : newLinksList) {
                        if (linkValidator.isLinkValid(link)) {
                            mLinksList.add(link);
                        }
                        if (link.isNsfw()) {
                            mNsfwImageCount++;
                        }
                    }
                } else {
                    Log.d(LOG_TAG, "linkDownloadCallback: Skipping Link validation");
                    // No LinkValidator was passed in, add all new Links
                    mLinksList.addAll(newLinksList);
                }

                // TODO: Use a LinkFilter here to remove duplicates
                // Find and remove any duplicate Links in the List
                mLinksList = removeDuplicateLinks(mLinksList);

                int newLinkCount = mLinksList.size() - oldLinkCount;
                Log.i(LOG_TAG, String.format("linkDownloadCallback: Downloaded %d new Links", newLinkCount));

                if (redditLinksCallback != null) {
                    redditLinksCallback.linksCallback(e);
                }
            }
        };
    }

    /**
     * Finds and removes all duplicate reddit Links in an ArrayList.
     *
     * @param linkList The ArrayList from which to remove duplicates.
     * @return An ArrayList of Link Objects containing no duplicates.
     */
    private ArrayList<Link> removeDuplicateLinks(ArrayList<Link> linkList) {
        // Use a LinkedHashSet to easily remove duplicates while keeping the order of the Links
        // TODO: Remove this method and use the new {@link DuplicateLinkFilter}
        return new ArrayList<>(new LinkedHashSet<>(linkList));
    }

    /**
     * Makes sure the current SubredditPaginator is active and loading links from the corrent
     * subreddit.
     *
     * @param subredditRequest The user-requested subreddit to load links from
     */
    private void validatePaginator(SubredditRequest subredditRequest) {
        Log.d(LOG_TAG, "validatePaginator() called with: " + "subredditRequest = [" + subredditRequest + "]");
        String subreddit = subredditRequest.getSubreddit();

        // The source of the data will be reddit, we'll save the listings and the list of links
        if (mSubredditPaginator == null) {
            Log.d(LOG_TAG, "Paginator was empty, instantiating a new one");

            // Are we loading the frontpage or a specific subreddit?
            if (subreddit.isEmpty()) {
                // Load the frontpage
                Log.i(LOG_TAG, "validatePaginator:  New Paginator for Frontpage");
                mSubredditPaginator = new SubredditPaginator(mRedditClient);
            } else {
                Log.i(LOG_TAG, "validatePaginator:  New Paginator for " + subreddit);
                mSubredditPaginator = new SubredditPaginator(mRedditClient, subreddit);
            }

            mSubredditPaginator.setLimit(subredditRequest.getLinkLimit());
            mSubredditPaginator.setSorting(subredditRequest.getSorting());
            mSubredditPaginator.setTimePeriod(subredditRequest.getTimePeriod());

        } else {
            Log.i(LOG_TAG, "validatePaginator: Using existing Paginator for " + subreddit);
            // TODO: Allow changing settings for existing paginator. This is to get rid of the
            // IllegalStateException. Might just need to set the Paginator to null and pass in a
            // "downloadNew" flag from the UI request
        }
    }
}
