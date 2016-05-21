package com.fernandobarillas.redditservice.tasks;

import android.os.AsyncTask;

import com.fernandobarillas.redditservice.callbacks.LinkDownloadCallback;
import com.fernandobarillas.redditservice.data.RedditData;
import com.fernandobarillas.redditservice.exceptions.NullRedditClientException;
import com.fernandobarillas.redditservice.models.Link;
import com.fernandobarillas.redditservice.requests.LinkDownloadRequest;
import com.orhanobut.logger.Logger;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to facilitate downloading new links from reddit using a background thread.
 */
public class LinkDownloadTask extends AsyncTask<LinkDownloadRequest, Void, Exception> {
    private static final String LOG_TAG = "LinkDownloadTask";

    private LinkDownloadRequest mLinkDownloadRequest;
    // Reddit data
    private List<Link>          mNewLinkList; // Holds the newly downloaded links

    protected Exception doInBackground(LinkDownloadRequest... linkDownloadRequests) {
        Logger.d("doInBackground()");
        if (linkDownloadRequests.length != 1) {
            return new Exception("More than 1 download request passed in");
        }

        mLinkDownloadRequest = linkDownloadRequests[0];
        RedditClient       mRedditClient      = mLinkDownloadRequest.getRedditClient();
        SubredditPaginator subredditPaginator = mLinkDownloadRequest.getSubredditPaginator();

        // Make sure that we have a client available for performing requests
        if (mRedditClient == null) {
            Logger.e("Reddit client was null, returning ");
            return new NullRedditClientException();
        }

        // The source of the data will be reddit, we'll save the listings and the list of links
        if (subredditPaginator == null) {
            Logger.d("Paginator was empty, instantiating a new one");
        }

        // mNewLinkList holds the new, filtered links to add to the main list
        mNewLinkList = new ArrayList<>();

        // Keep track of download retries
        int linkDownloadRetries = 1;

        String subreddit  = subredditPaginator.getSubreddit();
        int    index      = subredditPaginator.getPageIndex();
        String sorting    = "";
        String timePeriod = "";
        if (subredditPaginator.getSorting() != null) {
            sorting = subredditPaginator.getSorting()
                    .toString();
        }
        if (subredditPaginator.getTimePeriod() != null) {
            timePeriod = subredditPaginator.getTimePeriod()
                    .toString();
        }
        Logger.d(subreddit + " " + index + " " + sorting + " " + timePeriod);

        // Attempt to retry downloading the links
        while (mNewLinkList.size() == 0 && linkDownloadRetries <= RedditData.DOWNLOAD_RETRIES) {
            Logger.v("Download attempt number: " + linkDownloadRetries);

            // Get the links for the specific subreddit
            try {
                Listing<Submission> subredditListing  = subredditPaginator.next();
                List<Submission>    newSubredditLinks = subredditListing.getChildren();

                Logger.d("Downloaded " + newSubredditLinks.size() + " new links");

                if (newSubredditLinks.size() > 0) {
                    for (Submission submission : newSubredditLinks) {
                        mNewLinkList.add(new Link(submission));
                    }
                }
            } catch (Exception e) {
                Logger.e("doInBackground: Exception when downloading new links", e);
                return e;
            } finally {
                // Increment our retry count to know when to stop looping
                linkDownloadRetries++;
            }
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Logger.init(LOG_TAG);
    }

    /**
     * Executed when the download task is finished. This will add all the newly downloaded and
     * filtered Links to the main List, removing any duplicates found, and will ultimately execute a
     * onComplete method if set.
     */
    protected void onPostExecute(Exception e) {
        Logger.v("onPostExecute()");

        LinkDownloadCallback linkDownloadCallback = mLinkDownloadRequest.getLinkDownloadCallback();
        if (linkDownloadCallback == null) {
            return;
        }

        if (mNewLinkList == null) {
            // There was an error with the new Links, pass the error back
            Logger.e("onPostExecute: No new links could be downloaded", e);
            linkDownloadCallback.linkDownloadCallback(null, e);
            return;
        }

        Logger.i(String.format("onPostExecute: Downloaded %d new links", mNewLinkList.size()));

        // Execute the onComplete now that we have new data
        linkDownloadCallback.linkDownloadCallback(mNewLinkList, e);
    }
}
