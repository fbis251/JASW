package com.fernandobarillas.redditservice.observables;

import android.util.Log;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * An RxJava Observable that downloads a reddit User's subreddit subscriptions.
 */
public class UserSubredditsObservable {
    private static final String LOG_TAG = "UserSubredditsObs";

    private RedditClient mRedditClient;

    public UserSubredditsObservable(RedditClient redditClient) {
        Log.v(LOG_TAG,
              "UserSubredditsObservable() called with: " + "redditClient = [" + redditClient + "]");
        mRedditClient = redditClient;
    }

    public Observable<List<Subreddit>> getObservable() {
        return Observable.create(new Observable.OnSubscribe<List<Subreddit>>() {
            @Override
            public void call(Subscriber<? super List<Subreddit>> subscriber) {
                Log.v(LOG_TAG, "getObservable call() called with: " + "subscriber = [" +
                        subscriber + "]");
                UserSubredditsPaginator paginator =
                        new UserSubredditsPaginator(mRedditClient, "subscriber");
                // TODO: Will this request work?
                paginator.setLimit(100);
                try {
                    while (paginator.hasNext()) {
                        // Ensure we still have a subscriber to emit data to
                        if (subscriber.isUnsubscribed()) return;

                        Listing<Subreddit> subreddits = paginator.next(true);
                        if (subreddits != null && subreddits.getChildren() != null) {
                            // Pass on the subreddits as the network calls return them
                            subscriber.onNext(subreddits.getChildren());
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
