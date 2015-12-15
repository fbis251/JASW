package com.fernandobarillas.redditservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.fernandobarillas.redditservice.callbacks.RedditDataUpdatedCallback;
import com.fernandobarillas.redditservice.callbacks.RedditVoteCallback;
import com.fernandobarillas.redditservice.data.RedditData;
import com.fernandobarillas.redditservice.models.Link;
import com.fernandobarillas.redditservice.requests.SubredditRequest;

public class RedditService extends Service {
    public static final String REFRESH_TOKEN_KEY = "refresh_token";
    public static final String REDDIT_CLIENT_ID_KEY = "reddit_client_id";
    public static final String REDDIT_REDIRECT_URL_KEY = "reddit_redirect_url";
    private static final String LOG_TAG = "RedditService";
    private final IBinder mIBinder = new RedditBinder();
    private RedditData mRedditData;
    private long mStartTime;

    public RedditService() {
        Log.d(LOG_TAG, "RedditService()");
        // TODO: Handle mRedditData being null
    }

    private void initializeService(String refreshToken, String redditClientId, String redditRedirectUrl) {
        Log.d(LOG_TAG, "initializeService() refreshToken = [HIDDEN], redditClientId = [HIDDEN], redditRedirectUrl = [HIDDEN]");
        mStartTime = System.nanoTime();
        mRedditData = RedditData.newInstance(refreshToken, redditClientId, redditRedirectUrl);
        SubredditRequest subredditRequest = new SubredditRequest("all");

        if (mRedditData.getLinkCount() < 10) {
            mRedditData.getMoreLinks(subredditRequest,
                    new RedditDataUpdatedCallback() {
                        @Override
                        public void dataUpdateCallback(Exception e) {
                            if (e != null) {
                                Log.e(LOG_TAG, "dataUpdateCallback: Failed to get links", e);
                            }
                        }
                    });
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand() called with: " + "intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        String refreshToken = intent.getExtras().getString(REFRESH_TOKEN_KEY);
        String redditClientId = intent.getExtras().getString(REDDIT_CLIENT_ID_KEY);
        String redditRedirectUrl = intent.getExtras().getString(REDDIT_REDIRECT_URL_KEY);
        initializeService(refreshToken, redditClientId, redditRedirectUrl);

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind() called with: " + "intent = [" + intent + "]");
        String refreshToken = intent.getExtras().getString(REFRESH_TOKEN_KEY, "");
        String redditClientId = intent.getExtras().getString(REDDIT_CLIENT_ID_KEY, "");
        String redditRedirectUrl = intent.getExtras().getString(REDDIT_REDIRECT_URL_KEY, "");
        initializeService(refreshToken, redditClientId, redditRedirectUrl);

        return mIBinder;
    }

    public long getRunningTime() {
        return (System.nanoTime() - mStartTime) / 1000000000;
    }

    public void getMoreLinks(SubredditRequest subredditRequest, RedditDataUpdatedCallback redditDataUpdatedCallback) {
        Log.d(LOG_TAG, "getMoreLinks() called with: " + "redditDataUpdatedCallback = [" + redditDataUpdatedCallback + "]");
        mRedditData.getMoreLinks(subredditRequest, redditDataUpdatedCallback);
    }

    public int getLinkCount() {
        return mRedditData.getLinkCount();
    }

    public void getNewLinks(boolean getNewLinks) {
        mRedditData.getNewLinks(getNewLinks);
    }

    public Link getLink(int whichLink) {
        return mRedditData.getLink(whichLink);
    }

    public void upvoteLink(Link link, RedditVoteCallback voteCallback) {
        Log.d(LOG_TAG, "upvoteLink() called with: " + "link = [" + link + "], voteCallback = [" + voteCallback + "]");
        mRedditData.upvoteLink(link, voteCallback);
    }

    public void downvoteLink(Link link, RedditVoteCallback voteCallback) {
        Log.d(LOG_TAG, "downvoteLink() called with: " + "link = [" + link + "], voteCallback = [" + voteCallback + "]");
        mRedditData.downvoteLink(link, voteCallback);
    }

    public void removeVote(Link link, RedditVoteCallback voteCallback) {
        Log.d(LOG_TAG, "removeVote() called with: " + "link = [" + link + "], voteCallback = [" + voteCallback + "]");
        mRedditData.removeVote(link, voteCallback);
    }

    public class RedditBinder extends Binder {
        public RedditService getService() {
            Log.d(LOG_TAG, "getService()");
            return RedditService.this;
        }
    }
}
