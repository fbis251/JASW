package com.fernandobarillas.redditservice.requests;

import com.fernandobarillas.redditservice.callbacks.LinkDownloadCallback;

import net.dean.jraw.RedditClient;
import net.dean.jraw.paginators.SubredditPaginator;

import android.support.annotation.NonNull;

/**
 * Created by fb on 12/15/15.
 */
public class LinkDownloadRequest {
    RedditClient mRedditClient;
    SubredditPaginator mSubredditPaginator;
    LinkDownloadCallback mLinkDownloadCallback;

    public LinkDownloadRequest(@NonNull RedditClient redditClient,
                               @NonNull SubredditPaginator subredditPaginator,
                               LinkDownloadCallback linkDownloadCallback) {
        mRedditClient = redditClient;
        mSubredditPaginator = subredditPaginator;
        mLinkDownloadCallback = linkDownloadCallback;
    }

    public LinkDownloadCallback getLinkDownloadCallback() {
        return mLinkDownloadCallback;
    }

    public RedditClient getRedditClient() {
        return mRedditClient;
    }

    public SubredditPaginator getSubredditPaginator() {
        return mSubredditPaginator;
    }
}
