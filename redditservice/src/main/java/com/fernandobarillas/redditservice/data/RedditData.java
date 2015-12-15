package com.fernandobarillas.redditservice.data;

import android.text.TextUtils;
import android.util.Log;

import com.fernandobarillas.redditservice.callbacks.RedditAuthenticationCallback;
import com.fernandobarillas.redditservice.callbacks.RedditDataUpdatedCallback;
import com.fernandobarillas.redditservice.callbacks.RedditVoteCallback;
import com.fernandobarillas.redditservice.models.Link;
import com.fernandobarillas.redditservice.requests.AuthenticationRequest;
import com.fernandobarillas.redditservice.requests.SubredditRequest;
import com.fernandobarillas.redditservice.tasks.AuthenticationTask;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.LoggingMode;
import net.dean.jraw.http.UserAgent;

import java.util.List;

/**
 * Created by fb on 12/14/15.
 */
public class RedditData {
    public static final int DOWNLOAD_RETRIES = 3; // Download attempts before giving up
    private static final String LOG_TAG = "RedditData";
    private static RedditData sInstance;
    private RedditLinks mRedditLinks;
    private RedditAccount mRedditAccount;
    private RedditClient mRedditClient;

    private String mRefreshToken;
    private String mRedditClientId;
    private String mRedditRedirectUrl;

    private RedditData(String refreshToken, String redditClientId, String redditRedirectUrl) {
        Log.d(LOG_TAG, "RedditData()");

        if (TextUtils.isEmpty(redditClientId)) {
            Log.e(LOG_TAG, "RedditData: redditClientId is empty!");
        }

        mRefreshToken = refreshToken;
        mRedditClientId = redditClientId;
        mRedditRedirectUrl = redditRedirectUrl;

        // TODO: Need to be able to set a custom UserAgent!
        mRedditClient = new RedditClient(UserAgent.of("RedditServiceTest"));
        verifyAuthentication(null);

        mRedditLinks = RedditLinks.getInstance(mRedditClient);
        mRedditAccount = new RedditAccount(mRedditClient);

        // TODO: Handle setting logging mode when app isn't debuggable
        mRedditClient.setLoggingMode(LoggingMode.ALWAYS);
        mRedditClient.setRetryLimit(DOWNLOAD_RETRIES);
//        mRedditClient.hasActiveUserContext() // TODO: Check if user or app-only authenticated
    }

    public static RedditData getInstance(String refreshToken, String redditClientId, String redditRedirectUrl) {
        Log.d(LOG_TAG, "getInstance() refreshToken = [HIDDEN], redditClientId = [HIDDEN], redditRedirectUrl = [HIDDEN]");

        if (sInstance == null) {
            sInstance = new RedditData(refreshToken, redditClientId, redditRedirectUrl);
        }

        return sInstance;
    }

    public static RedditData newInstance(String refreshToken, String redditClientId, String redditRedirectUrl) {
        Log.d(LOG_TAG, "getInstance() refreshToken = [HIDDEN], redditClientId = [HIDDEN], redditRedirectUrl = [HIDDEN]");
        sInstance = null;
        return getInstance(refreshToken, redditClientId, redditRedirectUrl);
    }

    public List<Link> getRedditLinksList() {
        return mRedditLinks.getRedditLinksList();
    }

    public int getLinkCount() {
        return mRedditLinks.getCount();
    }

    public void getMoreLinks(final SubredditRequest subredditRequest, final RedditDataUpdatedCallback redditDataUpdatedCallback) {
        Log.d(LOG_TAG, "getMoreLinks() called with: " + "redditDataUpdatedCallback = [" + redditDataUpdatedCallback + "]");
        verifyAuthentication(new RedditAuthenticationCallback() {
            @Override
            public void authenticationCallback(Exception e) {
                mRedditLinks.getMoreLinks(subredditRequest, redditDataUpdatedCallback);
            }
        });
    }

    public void getNewLinks(final boolean getNewLinks) {
        Log.d(LOG_TAG, "getNewLinks() called with: " + "getNewLinks = [" + getNewLinks + "]");
        verifyAuthentication(new RedditAuthenticationCallback() {
            @Override
            public void authenticationCallback(Exception e) {
                mRedditLinks.getNewLinks(getNewLinks);
            }
        });
    }

    public Link getLink(int whichLink) {
        return mRedditLinks.getLink(whichLink);
    }

    public void upvoteLink(final Link link, final RedditVoteCallback voteCallback) {
        Log.d(LOG_TAG, "upvoteLink() called with: " + "link = [" + link + "], voteCallback = [" + voteCallback + "]");
        verifyAuthentication(new RedditAuthenticationCallback() {
            @Override
            public void authenticationCallback(Exception e) {
                mRedditAccount.upvoteLink(link, voteCallback);
            }
        });
    }

    public void downvoteLink(final Link link, final RedditVoteCallback voteCallback) {
        Log.d(LOG_TAG, "downvoteLink() called with: " + "link = [" + link + "], voteCallback = [" + voteCallback + "]");
        verifyAuthentication(new RedditAuthenticationCallback() {
            @Override
            public void authenticationCallback(Exception e) {
                mRedditAccount.downvoteLink(link, voteCallback);
            }
        });
    }

    public void removeVote(final Link link, final RedditVoteCallback voteCallback) {
        Log.d(LOG_TAG, "removeVote() called with: " + "link = [" + link + "], voteCallback = [" + voteCallback + "]");
        verifyAuthentication(new RedditAuthenticationCallback() {
            @Override
            public void authenticationCallback(Exception e) {
                mRedditAccount.removeVote(link, voteCallback);
            }
        });
    }

    // TODO: This method needs to block before the requests are let through
    private synchronized void verifyAuthentication(RedditAuthenticationCallback authenticationCallback) {
        Log.d(LOG_TAG, "verifyAuthentication() is authenticated: " + mRedditClient.isAuthenticated());
        if (!mRedditClient.isAuthenticated()) {
            AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                    mRedditClient,
                    mRefreshToken,
                    mRedditClientId,
                    mRedditRedirectUrl,
                    authenticationCallback);
            AuthenticationTask authenticationTask = new AuthenticationTask();
            authenticationTask.execute(authenticationRequest);
        } else {
            // RedditClient is properly authenticated, run callback with no Exception
            authenticationCallback.authenticationCallback(null);
        }
    }
}
