package com.fernandobarillas.redditservice.data;

import android.util.Log;

import com.fernandobarillas.redditservice.callbacks.RedditLinksCallback;
import com.fernandobarillas.redditservice.models.Link;
import com.fernandobarillas.redditservice.observables.UserSubscriptions;
import com.fernandobarillas.redditservice.requests.SubredditRequest;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.LoggingMode;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.VoteDirection;

import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by fb on 12/14/15.
 */
public class RedditData {
    public static final  int    DOWNLOAD_RETRIES = 3; // Download attempts before giving up
    private static final String LOG_TAG          = "RedditData";
    private static RedditData    sInstance;
    public         RedditClient  mRedditClient;
    private        RedditLinks   mRedditLinks;
    private        RedditAccount mRedditAccount;

    private UserAgent mUserAgent;

    // TODO: Enable this:
//    private HashMap<String, RedditLinks> mRedditLinksHashMap;

    private RedditData(UserAgent userAgent) {
        Log.d(LOG_TAG, "RedditData()");

        mUserAgent = userAgent;

        mRedditClient = new RedditClient(mUserAgent);
        // TODO: Handle setting logging mode when app isn't debuggable
//        mRedditClient.setLoggingMode(LoggingMode.ALWAYS);
        mRedditClient.setLoggingMode(LoggingMode.ON_FAIL);
        mRedditClient.setRetryLimit(DOWNLOAD_RETRIES);

        mRedditLinks = new RedditLinks(mRedditClient);
        mRedditAccount = new RedditAccount(mRedditClient);
    }

    public static RedditData getInstance(UserAgent userAgent) {
        Log.v(LOG_TAG, "getInstance() called with: " + "userAgent = [" + userAgent + "]");

        if (sInstance == null) {
            sInstance = new RedditData(userAgent);
        }

        return sInstance;
    }

    public static RedditData newInstance(UserAgent userAgent) {
        Log.v(LOG_TAG, "newInstance() called with: " + "userAgent = [" + userAgent + "]");
        sInstance = null;
        return getInstance(userAgent);
    }

    public int getLastViewedLink() {
        return mRedditLinks.getLastViewedLink();
    }

    public void setLastViewedLink(int lastViewedLink) {
        mRedditLinks.setLastViewedLink(lastViewedLink);
    }

    public Link getLink(int whichLink) {
        return mRedditLinks.getLink(whichLink);
    }

    public int getLinkCount() {
        return mRedditLinks.getCount();
    }

    public Observable<String> getLinksObservable(final SubredditRequest subredditRequest) {
        return Observable.create(new Observable.OnSubscribe<String>() {

            @Override
            public void call(Subscriber<? super String> subscriber) {
                Log.v(LOG_TAG, "getLinksObservable() call() called with: " + "subscriber = [" + subscriber + "]");
                if (subscriber.isUnsubscribed()) return;

                int attempts = 0;
                while (attempts++ < DOWNLOAD_RETRIES) {
                    mRedditLinks.getNewLinks(subredditRequest);
                    subscriber.onNext("Yup");
                }

                subscriber.onCompleted();
            }
        });
    }

    public void getMoreLinks(final RedditLinksCallback linksCallback) {
        Log.v(LOG_TAG, "getMoreLinks()");
        mRedditLinks.getMoreLinks(linksCallback);
    }

    public void getNewLinks(final SubredditRequest subredditRequest) {
        Log.v(LOG_TAG, "getNewLinks() called with: " + "subredditRequest = [" + subredditRequest + "]");
        int attempts = 0;
        while (attempts++ < DOWNLOAD_RETRIES) {
            mRedditLinks.getNewLinks(subredditRequest);
        }
    }

    public int getNsfwImageCount() {
        return mRedditLinks.getNsfwImageCount();
    }

    public List<Link> getRedditLinksList() {
        return mRedditLinks.getRedditLinksList();
    }

    public Observable<Subreddit> getSubscriptions() {
        UserSubscriptions subredditsObservable = new UserSubscriptions(mRedditClient);
        return subredditsObservable.getSubscriptions();
    }

    public Observable<Boolean> saveLink(final Link link, final boolean isSave) {
        return mRedditAccount.saveLink(link, isSave);
    }

    public Observable<Boolean> voteLink(final Link link, final VoteDirection voteDirection) {
        return mRedditAccount.voteLink(link, voteDirection);
    }
}
