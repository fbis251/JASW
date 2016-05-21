package com.fernandobarillas.redditservice.observables;

import android.util.Log;

import com.fernandobarillas.redditservice.data.RedditLinks;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import rx.Observable;
import rx.Subscriber;

/**
 * An RxJava Observable that downloads a reddit User's subreddit subscriptions.
 */
public class UserSubscriptions {
    private static final String LOG_TAG = "UserSubredditsObs";

    private RedditClient mRedditClient;

    public UserSubscriptions(RedditClient redditClient) {
        Log.v(LOG_TAG, "UserSubscriptions() called with: " + "redditClient = [" + redditClient + "]");
        mRedditClient = redditClient;
    }

    public Observable<Subreddit> getSubscriptions() {
        return Observable.create(new Observable.OnSubscribe<Subreddit>() {
            @Override
            public void call(Subscriber<? super Subreddit> subscriber) {
                Log.v(LOG_TAG, "downloadSubscriptions()");
                UserSubredditsPaginator paginator = new UserSubredditsPaginator(mRedditClient, "subscriber");
                paginator.setLimit(RedditLinks.MAX_LINK_LIMIT);
                try {
                    while (paginator.hasNext()) {
                        // Ensure we still have a subscriber to emit data to

                        Listing<Subreddit> subreddits = paginator.next(true);
                        if (subreddits != null && subreddits.getChildren() != null) {
                            // Pass on the subreddits as the network calls return them
                            for (Subreddit subreddit : subreddits) {
                                subscriber.onNext(subreddit);
                            }
                        }
                    }

                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(e);
                    }
                }
            }
        });
    }
}
