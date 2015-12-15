package com.fernandobarillas.redditservice.data;

import android.os.AsyncTask;
import android.util.Log;

import com.fernandobarillas.redditservice.callbacks.RedditDataUpdatedCallback;
import com.fernandobarillas.redditservice.exceptions.AuthenticationException;
import com.fernandobarillas.redditservice.models.Link;
import com.fernandobarillas.redditservice.requests.SubredditRequest;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * A class to easily download Links using the reddit API. The Links are verified for validity and
 * all duplicates are removed. Downloads happen in a background thread to prevent blocking. There
 * is also support for downloading posts from specific subreddits and sorting the Links using
 * reddit's API (Hot, New, Top, etc.), and filtering out any posts marked as NSFW.
 */
public class RedditLinks {
    public static final int LINK_LIMIT = 100;
    // Logging
    private static final String LOG_TAG = RedditLinks.class.getSimpleName();
    // Singleton pattern
    private static RedditLinks sInstance;

    // Reddit client and data
    private ArrayList<Link> mLinkList;

    private RedditClient mRedditClient;

    private SubredditPaginator mSubredditPaginator;

    private LinkDownloadTask mLinkDownloadTask;

    private boolean mGetNewLinks;

    private int mNsfwImageCount; // How many nsfw images were downloaded

    /**
     * This constructor sets the default options to be used, in addition to loading user-set
     * preferences before any links are downloaded from reddit.
     *
     * @param redditClient The client to use when downloading links
     */
    private RedditLinks(RedditClient redditClient) {
        Log.d(LOG_TAG, "RedditLinks() called with: " + "redditClient = [" + redditClient + "]");
        mRedditClient = redditClient;
        mLinkList = new ArrayList<>();
        mGetNewLinks = true;
        mSubredditPaginator = null;
        mNsfwImageCount = 0;
    }

    /**
     * Returns a Singleton instance of a RedditLinks object.
     *
     * @param redditClient The client to use when downloading links
     * @return A Singleton instance of a RedditLinks object.
     */
    public static RedditLinks getInstance(RedditClient redditClient) {
        Log.d(LOG_TAG, "getInstance() called with: " + "redditClient = [" + redditClient + "]");
        if (sInstance == null) {
            Log.i(LOG_TAG, "Creating a new instance of RedditLinks");
            sInstance = new RedditLinks(redditClient);
        }

        return sInstance;
    }

    /**
     * Creates a new instance of a RedditLinks object.
     *
     * @param redditClient The client to use when downloading links
     * @return A new instance of a RedditLinks object.
     */
    public static RedditLinks newInstance(RedditClient redditClient) {
        Log.d(LOG_TAG, "newInstance() called with: " + "redditClient = [" + redditClient + "]");
        sInstance = null;
        return getInstance(redditClient);
    }

    /**
     * Executes a callback method. Used in {@link LinkDownloadTask} in the onPostExecute method.
     * This method is only meant to be executed when the download task has successfully added new
     * Links.
     *
     * @param redditDataUpdatedCallback The callback to execute
     */
    private void executeCallback(Exception e,
                                 final RedditDataUpdatedCallback redditDataUpdatedCallback) {
        Log.v(LOG_TAG, "executeCallback()");

        // Make sure a callback has been set
        if (redditDataUpdatedCallback == null) {
            return;
        }

        // Now that we know the callback isn't null, execute it
        redditDataUpdatedCallback.dataUpdateCallback(e);
    }

    /**
     * This method initiates the AsyncTask that downloads new reddit Links. This method is
     * synchronized because we don't want to have more than one download task executing at a time.
     * For example, this helps prevent excessive downloads from happening as a user swipes through
     * the gallery.
     *
     * @param redditDataUpdatedCallback A callback to execute when the download is completed.
     */
    private synchronized void executeLinkDownload(
            SubredditRequest subredditRequest,
            RedditDataUpdatedCallback redditDataUpdatedCallback) {
        Log.v(LOG_TAG, "executeLinkDownload()");

        // Create a new download task to get more Links from Reddit.
        mLinkDownloadTask = new LinkDownloadTask();
        mLinkDownloadTask.setCallback(redditDataUpdatedCallback);
        mLinkDownloadTask.execute(subredditRequest);
    }

    /**
     * Gets the number of Links the app has downloaded from Reddit.
     *
     * @return The total count of image Links the app has downloaded.
     */
    public int getCount() {
        return mLinkList.size();
    }

    /**
     * Gets a specific reddit Link from the List.
     *
     * @param whichLink The ID of the Link to return.
     * @return The Link with ID whichLink.
     */
    public Link getLink(int whichLink) {
        if (whichLink < mLinkList.size() && whichLink >= 0) {
            return mLinkList.get(whichLink);
        }

        return null;
    }

    /**
     * This method verifies that we are not currently attempting to download reddit links. Because
     * executeLinkDownload is a synchronized method, we do not want to queue more calls to it than
     * necessary.
     *
     * @param redditDataUpdatedCallback A callback to execute when the download is completed.
     */
    // TODO: Add cancelDownload boolean to force a cancel of a pending download
    public void getMoreLinks(SubredditRequest subredditRequest, RedditDataUpdatedCallback redditDataUpdatedCallback) {
        Log.v(LOG_TAG, "getMoreLinks()");

        if (mLinkDownloadTask != null
                && mLinkDownloadTask.getStatus() == AsyncTask.Status.RUNNING) {
            Log.w(LOG_TAG, "DownloadFilesTask Already downloading data, returning");

            return;
        }

        executeLinkDownload(subredditRequest, redditDataUpdatedCallback);
    }

    /**
     * Notifies the LinkDownloadTask to get new links instead of getting more links from reddit.
     *
     * @param getNewLinks True to get new Links, discarding the Links already downloaded.
     *                    False to get more links in addition to the ones already downloaded.
     */
    public void getNewLinks(boolean getNewLinks) {
        mGetNewLinks = getNewLinks;

        if (mGetNewLinks) {
            // Discard the old List to force a download of new data
            mLinkList = new ArrayList<>();
        }

        // Cancel the current download task since we will be downloading new links
        if (mLinkDownloadTask != null) {
            Log.i(LOG_TAG, "Cancelling pending link download");
            mLinkDownloadTask.cancel(true);
            mLinkDownloadTask = null;
        }
    }

    /**
     * Gets the number of NSFW images that were downloaded in the last request. Useful when no
     * "valid" links were downloaded and the user has NSFW images disabled. This will allow the
     * user
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

        return mLinkList;
    }

    /**
     * A class to facilitate downloading new links from reddit using a background thread.
     */
    // TODO: Needs refactoring into its own Class file
    class LinkDownloadTask extends AsyncTask<SubredditRequest, Void, Exception> {
        private static final String LOG_TAG = "LinkDownloadTask";

        // Reddit data
        private List<Link> mNewLinkList; // Holds the newly downloaded links
        private RedditDataUpdatedCallback mRedditDataUpdatedCallback;

        protected Exception doInBackground(SubredditRequest... subredditRequests) {
            Log.d(LOG_TAG, "doInBackground()");
            if (subredditRequests.length != 1) {
                return null;
            }

            return downloadMoreLinks(subredditRequests[0]);
        }

        /**
         * Executed when the download task is finished. This will add all the newly downloaded and
         * filtered Links to the main List, removing any duplicates found, and will ultimately
         * execute a callback method if set.
         */
        protected void onPostExecute(Exception e) {
            Log.v(LOG_TAG, "onPostExecute()");

            if (mGetNewLinks) {
                // We are done getting the fresh data from reddit as requested
                mGetNewLinks = false;
            }

            if (mNewLinkList == null) {
                // There was an error with the new Links, pass the error back
                executeCallback(e, mRedditDataUpdatedCallback);
                return;
            }

            // Add all the newly gotten Links to the main List
            mLinkList.addAll(mNewLinkList);

            // Find and remove any duplicate Links in the List
            mLinkList = removeDuplicateLinks(mLinkList);

            Log.i(LOG_TAG, "Valid image links: " + mNewLinkList.size());
            Log.i(LOG_TAG, "Total links in list: " + mLinkList.size());

            // Execute the callback now that we have new data
            executeCallback(e, mRedditDataUpdatedCallback);
        }

        /**
         * Downloads more links from reddit, filtering them to find valid image links.
         *
         * @return The number of valid, new links downloaded
         */
        private Exception downloadMoreLinks(SubredditRequest subredditRequest) {
            Log.v(LOG_TAG, "downloadMoreLinks()");

            // Make sure that we have a client available for performing requests
            if (mRedditClient == null) {
                Log.e(LOG_TAG, "Reddit client was null, returning ");
                return null;
            }

            String mSubreddit = subredditRequest.getSubreddit();

            if (!subredditRequest.getSubreddit().equals(mSubreddit)) {
                mGetNewLinks = true;
                mSubreddit = subredditRequest.getSubreddit();
            }

            if (mGetNewLinks) {
                // Discard our old paginator since we want fresh data from reddit.
                Log.v(LOG_TAG, "Getting brand new links");
                mSubredditPaginator = null;
            }

            // The source of the data will be reddit, we'll save the listings and the list of links
            if (mSubredditPaginator == null) {
                Log.d(LOG_TAG, "Paginator was empty, instantiating a new one");

                // Are we loading the frontpage or a specific subreddit?
                if (mSubreddit.isEmpty()) {
                    // Load the frontpage
                    Log.v(LOG_TAG, "Links from subreddit: Frontpage");
                    mSubredditPaginator = new SubredditPaginator(mRedditClient);
                } else {
                    Log.v(LOG_TAG, "Links from subreddit: " + mSubreddit);
                    mSubredditPaginator = new SubredditPaginator(mRedditClient, mSubreddit);
                }

                mSubredditPaginator.setLimit(subredditRequest.getLinkLimit());
                mSubredditPaginator.setSorting(subredditRequest.getSorting());
                mSubredditPaginator.setTimePeriod(subredditRequest.getTimePeriod());
            }

            // mNewLinkList holds the new, filtered links to add to the main list
            mNewLinkList = new ArrayList<>();
            mNsfwImageCount = 0; // Reset the count for every new request

            // Keep track of download retries
            int linkDownloadRetries = 1;

            Log.d(LOG_TAG, "Paginator: " +
                            mSubredditPaginator.getSubreddit() + " " +
                            mSubredditPaginator.getPageIndex() + " " +
                            mSubredditPaginator.getSorting() + " " +
                            mSubredditPaginator.getTimePeriod()
            );

            if (!mRedditClient.isAuthenticated()) {
                // RedditClient was not authenticated
                return new AuthenticationException();
            }

            // Attempt to retry downloading the links
            while (mNewLinkList.size() == 0 && linkDownloadRetries <= RedditData.DOWNLOAD_RETRIES) {
                Log.v(LOG_TAG, "Download attempt number: " + linkDownloadRetries);

                // Get the links for the specific subreddit
                try {
                    Listing<Submission> subredditListing = mSubredditPaginator.next();
                    List<Submission> newSubredditLinks = subredditListing.getChildren();

                    Log.d(LOG_TAG, "Downloaded " + newSubredditLinks.size() + " new links");

                    if (newSubredditLinks.size() > 0) {
                        for (Submission submission : newSubredditLinks) {
                            Link link = new Link(submission);
                            // Only add valid image links
                            // TODO: Allow using a custom LinkValidator for custom filtering
                            if (isLinkValid(link, subredditRequest.isShowNsfwEnabled())) {
                                mNewLinkList.add(link);
                            } else if (link.isNsfw()) {
                                mNsfwImageCount++;
                            }
                        }
                    }
                } catch (Exception e) {
                    return e;
                } finally {
                    // Increment our retry count to know when to stop looping
                    linkDownloadRetries++;
                }
            }

            return null;
        }

        /**
         * Checks a Link to make sure it contains a valid image or album URL.
         *
         * @param link The Link to check for validity
         * @return True if the Link contains a valid image or album URL.
         */
        private boolean isLinkValid(Link link, boolean showNsfw) {
            // Don't display links marked as NSFW unless the user wants to
            if (link.isNsfw() && !showNsfw) {
                return false;
            }

            // TODO: Temporarily disallow imgur album links
            String[] domainBlacklist = {
                    "http://imgur.com/a/",
                    "https://imgur.com/a/",
                    "http://imgur.com/gallery/",
                    "https://imgur.com/gallery/",
            };

            String[] extensionBlacklist = {
                    ".gif",
                    ".gifv",
            };

            String[] extensionWhitelist = {
                    ".jpg",
                    ".jpeg",
                    ".png",
                    ".bmp",
            };

            // TODO: Perform better filtering here
            String linkUrl = link.getUrl();

            for (String extension : extensionBlacklist) {
                if (linkUrl.endsWith(extension)) {
                    return false;
                }
            }

            for (String domain : domainBlacklist) {
                if (linkUrl.startsWith(domain)) {
                    return false;
                }
            }

            for (String extension : extensionWhitelist) {
                if (linkUrl.endsWith(extension)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Finds and removes all duplicate reddit Links in an ArrayList.
         *
         * @param linkList The ArrayList from which to remove duplicates.
         * @return An ArrayList of Link Objects containing no duplicates.
         */
        private ArrayList<Link> removeDuplicateLinks(ArrayList<Link> linkList) {
            // Use a LinkedHashSet to easily remove duplicates while keeping the order of the Links
            return new ArrayList<>(new LinkedHashSet<>(linkList));
        }

        /**
         * Sets a callback method to execute when the download is completed.
         *
         * @param redditDataUpdatedCallback The method to execute.
         */
        private void setCallback(RedditDataUpdatedCallback redditDataUpdatedCallback) {
            mRedditDataUpdatedCallback = redditDataUpdatedCallback;
        }
    }
}
