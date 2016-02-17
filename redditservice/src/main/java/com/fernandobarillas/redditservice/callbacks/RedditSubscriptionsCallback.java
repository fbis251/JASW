package com.fernandobarillas.redditservice.callbacks;

import net.dean.jraw.models.Subreddit;

import java.util.HashSet;

/**
 * Created by fb on 2/14/16.
 */
public interface RedditSubscriptionsCallback {
    void onComplete(HashSet<Subreddit> subreddits);
    void onError(Throwable e);
}
