package com.fernandobarillas.redditservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.fernandobarillas.redditservice.callbacks.RedditLinksCallback;
import com.fernandobarillas.redditservice.callbacks.RedditSaveCallback;
import com.fernandobarillas.redditservice.callbacks.RedditSubscriptionsCallback;
import com.fernandobarillas.redditservice.callbacks.RedditVoteCallback;
import com.fernandobarillas.redditservice.data.RedditData;
import com.fernandobarillas.redditservice.models.Link;
import com.fernandobarillas.redditservice.preferences.ServicePreferences;
import com.fernandobarillas.redditservice.requests.SubredditRequest;

import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.VoteDirection;

import java.util.HashSet;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RedditService extends Service {
    public static final  String REDDIT_BASE_URL         = "https://reddit.com";
    public static final  String USER_AGENT_KEY          = "user_agent";
    public static final  String REFRESH_TOKEN_KEY       = "refresh_token";
    public static final  String REDDIT_CLIENT_ID_KEY    = "reddit_client_id";
    public static final  String REDDIT_REDIRECT_URL_KEY = "reddit_redirect_url";
    public static final  int    RELOAD_LIMIT            = 50; // How many links cached before a reload is triggered
    private static final String LOG_TAG                 = "RedditService";

    private final IBinder mIBinder = new RedditBinder();

    private ServicePreferences mServicePreferences;
    private RedditData         mRedditData;

    public RedditService() {
        Log.d(LOG_TAG, "RedditService() instantiated");
        if (mServicePreferences == null) return;
        // TODO: Allow the redditService to handle the instance of a subredditRequest to allow easy access to a single subreddit paginator

        Log.d("RedditService", "Getting RedditData instance");
        mRedditData = RedditData.getInstance(UserAgent.of(mServicePreferences.getUserAgentString()),
                                             mServicePreferences.getRefreshToken(),
                                             mServicePreferences.getRedditClientId(),
                                             mServicePreferences.getRedditRedirectUrl());
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind() called with: " + "intent = [" + intent + "]");
        initializeService(intent);

        return mIBinder;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG,
              "onStartCommand() called with: " + "intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        initializeService(intent);

        return Service.START_NOT_STICKY;
    }

    public void downvoteLink(Link link, RedditVoteCallback voteCallback) {
        Log.v(LOG_TAG,
              "downvoteLink() called with: " + "link = [" + link + "], voteCallback = [" + voteCallback + "]");
        mRedditData.downvoteLink(link, voteCallback);
    }

    public int getLastViewedLink() {
        return mRedditData.getLastViewedLink();
    }

    public void setLastViewedLink(int lastViewedLink) {
        mRedditData.setLastViewedLink(lastViewedLink);
    }

    public Link getLink(int whichLink) {
        return mRedditData.getLink(whichLink);
    }

    public int getLinkCount() {
        return mRedditData.getLinkCount();
    }

    public int getNsfwImageCount() {
        return mRedditData.getNsfwImageCount();
    }

    public void getMoreLinks(final RedditLinksCallback linksCallback) {
        Log.v(LOG_TAG, "getMoreLinks() called with: " + "linksCallback = [" + linksCallback + "]");
        mRedditData.getMoreLinks(linksCallback);
    }

    public void getNewLinks(final SubredditRequest subredditRequest) {
        Log.v(LOG_TAG,
              "getNewLinks() called with: " + "subredditRequest = [" + subredditRequest + "]");
        mRedditData.getNewLinks(subredditRequest);
    }

    public void removeVote(Link link, RedditVoteCallback voteCallback) {
        Log.d(LOG_TAG,
              "removeVote() called with: " + "link = [" + link + "], voteCallback = [" + voteCallback + "]");
        mRedditData.removeVote(link, voteCallback);
    }

    public void saveLink(final Link link, final RedditSaveCallback saveCallback) {
        Log.v(LOG_TAG,
              "saveLink() called with: " + "link = [" + link + "], saveCallback = [" + saveCallback + "]");
        mRedditData.saveLink(link, saveCallback);
    }

    public void unsaveLink(final Link link, final RedditSaveCallback saveCallback) {
        Log.v(LOG_TAG,
              "unsaveLink() called with: " + "link = [" + link + "], saveCallback = [" + saveCallback + "]");
        mRedditData.unsaveLink(link, saveCallback);
    }

    public void upvoteLink(Link link, RedditVoteCallback voteCallback) {
        Log.d(LOG_TAG,
              "upvoteLink() called with: " + "link = [" + link + "], voteCallback = [" + voteCallback + "]");
        mRedditData.upvoteLink(link, voteCallback);
    }

    public void voteLink(final Link link,
                         final VoteDirection voteDirection,
                         final RedditVoteCallback voteCallback) {
        mRedditData.voteLink(link, voteDirection, voteCallback);
    }

    public Subscription getUserSubreddits(final RedditSubscriptionsCallback subscriptionsCallback) {
        Observable<List<Subreddit>> subredditsObservable = mRedditData.getUserSubredditsObservable();
        final HashSet<Subreddit> subredditsSet = new HashSet<>();
        return subredditsObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Subreddit>>() {
                    @Override
                    public void onCompleted() {
                        Log.v(LOG_TAG,
                              "getUserSubreddits onCompleted: Set size: " + subredditsSet.size());
                        if (subscriptionsCallback != null) {
                            subscriptionsCallback.onComplete(subredditsSet);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, "getUserSubreddits onError: ", e);

                        if (subscriptionsCallback != null) {
                            subscriptionsCallback.onError(e);
                        }
                    }

                    @Override
                    public void onNext(List<Subreddit> subreddits) {
                        Log.v(LOG_TAG,
                              "getUserSubreddits onNext() called with: " + "subreddits = [" +
                                      subreddits + "]");
                        subredditsSet.addAll(subreddits);
                    }
                });
    }

    private void initializeService(Intent intent) {
        Log.v(LOG_TAG, "initializeService() called with: " + "intent = [" + intent + "]");

        Context serviceContext = this;
        mServicePreferences = new ServicePreferences(serviceContext);

        String userAgentString   = intent.getExtras().getString(USER_AGENT_KEY, "");
        String refreshToken      = intent.getExtras().getString(REFRESH_TOKEN_KEY);
        String redditClientId    = intent.getExtras().getString(REDDIT_CLIENT_ID_KEY);
        String redditRedirectUrl = intent.getExtras().getString(REDDIT_REDIRECT_URL_KEY);

        mServicePreferences.setUserAgentString(userAgentString);
        mServicePreferences.setRefreshToken(refreshToken);
        mServicePreferences.setRedditClientId(redditClientId);
        mServicePreferences.setRedditRedirectUrl(redditRedirectUrl);

        // TODO: Handle null extras
        mRedditData = RedditData.getInstance(UserAgent.of(mServicePreferences.getUserAgentString()),
                                             mServicePreferences.getRefreshToken(),
                                             mServicePreferences.getRedditClientId(),
                                             mServicePreferences.getRedditRedirectUrl());

        // TODO: Force new instance of reddit data for new user logins/user switching
    }

    public class RedditBinder extends Binder {
        public RedditService getService() {
            Log.d(LOG_TAG, "getService()");
            return RedditService.this;
        }
    }
}
