package com.fernandobarillas.redditservice.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.fernandobarillas.redditservice.callbacks.LinkDownloadCallback;
import com.fernandobarillas.redditservice.data.RedditData;
import com.fernandobarillas.redditservice.exceptions.AuthenticationException;
import com.fernandobarillas.redditservice.exceptions.NullRedditClientException;
import com.fernandobarillas.redditservice.models.Link;
import com.fernandobarillas.redditservice.requests.LinkDownloadRequest;

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
    private List<Link> mNewLinkList; // Holds the newly downloaded links

    protected Exception doInBackground(LinkDownloadRequest... linkDownloadRequests) {
        Log.d(LOG_TAG, "doInBackground()");
        if (linkDownloadRequests.length != 1) {
            return new Exception("More than 1 download request passed in");
        }

        mLinkDownloadRequest = linkDownloadRequests[0];
        RedditClient mRedditClient = mLinkDownloadRequest.getRedditClient();
        SubredditPaginator subredditPaginator = mLinkDownloadRequest.getSubredditPaginator();

        // Make sure that we have a client available for performing requests
        if (mRedditClient == null) {
            Log.e(LOG_TAG, "Reddit client was null, returning ");
            return new NullRedditClientException();
        }

        if (!mRedditClient.isAuthenticated()) {
            // RedditClient was not authenticated
            return new AuthenticationException();
        }

        // The source of the data will be reddit, we'll save the listings and the list of links
        if (subredditPaginator == null) {
            Log.d(LOG_TAG, "Paginator was empty, instantiating a new one");

        }

        // mNewLinkList holds the new, filtered links to add to the main list
        mNewLinkList = new ArrayList<>();

        // Keep track of download retries
        int linkDownloadRetries = 1;

        Log.d(LOG_TAG, "Paginator: " +
                subredditPaginator.getSubreddit() + " " +
                subredditPaginator.getPageIndex() + " " +
                subredditPaginator.getSorting() + " " +
                subredditPaginator.getTimePeriod());

        // Attempt to retry downloading the links
        while (mNewLinkList.size() == 0 && linkDownloadRetries <= RedditData.DOWNLOAD_RETRIES) {
            Log.v(LOG_TAG, "Download attempt number: " + linkDownloadRetries);

            // Get the links for the specific subreddit
            try {
                Listing<Submission> subredditListing = subredditPaginator.next();
                List<Submission> newSubredditLinks = subredditListing.getChildren();

                Log.d(LOG_TAG, "Downloaded " + newSubredditLinks.size() + " new links");

                if (newSubredditLinks.size() > 0) {
                    for (Submission submission : newSubredditLinks) {
                        mNewLinkList.add(new Link(submission));
                    }
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "doInBackground: Exception when downloading new links", e);
                return e;
            } finally {
                // Increment our retry count to know when to stop looping
                linkDownloadRetries++;
            }
        }

        return null;
    }

    /**
     * Executed when the download task is finished. This will add all the newly downloaded and
     * filtered Links to the main List, removing any duplicates found, and will ultimately execute a
     * onComplete method if set.
     */
    protected void onPostExecute(Exception e) {
        Log.v(LOG_TAG, "onPostExecute()");

        LinkDownloadCallback linkDownloadCallback = mLinkDownloadRequest.getLinkDownloadCallback();
        if (linkDownloadCallback == null) {
            return;
        }

        if (mNewLinkList == null) {
            // There was an error with the new Links, pass the error back
            Log.e(LOG_TAG, "onPostExecute: No new links could be downloaded", e);
            linkDownloadCallback.linkDownloadCallback(null, e);
            return;
        }

        Log.i(LOG_TAG, String.format("onPostExecute: Downloaded %d new links", mNewLinkList.size()));

        // Execute the onComplete now that we have new data
        linkDownloadCallback.linkDownloadCallback(mNewLinkList, e);
    }
}
