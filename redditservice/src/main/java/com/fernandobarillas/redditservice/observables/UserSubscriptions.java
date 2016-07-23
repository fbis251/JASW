package com.fernandobarillas.redditservice.observables;

import com.fernandobarillas.redditservice.RedditService;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

/**
 * An RxJava Observable that downloads a reddit User's subreddit subscriptions.
 */
public class UserSubscriptions {

    private RedditClient mRedditClient;

    public UserSubscriptions(RedditClient redditClient) {
        Timber.v("UserSubscriptions() called with: " + "redditClient = [" + redditClient + "]");
        mRedditClient = redditClient;
    }

    public Observable<List<Subreddit>> getSubscriptions() {
        Timber.v("getSubscriptions() called");
        return Observable.create(new Observable.OnSubscribe<List<Subreddit>>() {
            @Override
            public void call(Subscriber<? super List<Subreddit>> subscriber) {
                Timber.v("getSubscriptions call() called with: "
                        + "subscriber = ["
                        + subscriber
                        + "]");
                UserSubredditsPaginator paginator =
                        new UserSubredditsPaginator(mRedditClient, "subscriber");
                paginator.setLimit(RedditService.MAX_LINK_LIMIT);
                try {
                    while (paginator.hasNext()) {
                        // Ensure we still have a subscriber to emit data to
                        Listing<Subreddit> subreddits = paginator.next(true);
                        if (subreddits != null && subreddits.getChildren() != null) {
                            // Pass on the subreddits as the network calls return them
                            subscriber.onNext(subreddits.getChildren());
                        }
                    }

                    if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
                } catch (Exception e) {
                    if (!subscriber.isUnsubscribed()) subscriber.onError(e);
                }
            }
        });
    }
}
