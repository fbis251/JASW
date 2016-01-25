package com.fernandobarillas.redditservice.data;

import android.text.TextUtils;
import android.util.Log;

import com.fernandobarillas.redditservice.callbacks.RedditAuthenticationCallback;
import com.fernandobarillas.redditservice.callbacks.RedditLinksCallback;
import com.fernandobarillas.redditservice.callbacks.RedditSaveCallback;
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

    private UserAgent mUserAgent;
    private String mRefreshToken;
    private String mRedditClientId;
    private String mRedditRedirectUrl;

    private RedditData(UserAgent userAgent, String refreshToken, String redditClientId,
                       String redditRedirectUrl) {
        Log.d(LOG_TAG, "RedditData()");

        if (TextUtils.isEmpty(redditClientId)) {
            Log.e(LOG_TAG, "RedditData: redditClientId is empty!");
        }

        mUserAgent = userAgent;
        mRefreshToken = refreshToken;
        mRedditClientId = redditClientId;
        mRedditRedirectUrl = redditRedirectUrl;

        mRedditClient = new RedditClient(mUserAgent);
        // TODO: Handle setting logging mode when app isn't debuggable
        mRedditClient.setLoggingMode(LoggingMode.ALWAYS);
//        mRedditClient.setLoggingMode(LoggingMode.ON_FAIL);
        mRedditClient.setRetryLimit(DOWNLOAD_RETRIES);

//        mRedditClient.hasActiveUserContext() // TODO: Check if user or app-only authenticated
//
//        verifyAuthentication(null);
        mRedditLinks = new RedditLinks(mRedditClient);
        mRedditAccount = new RedditAccount(mRedditClient);
//        verifyAuthentication(new RedditAuthenticationCallback() {
//            @Override
//            public void authenticationCallback(Exception e) {
//                mRedditLinks = RedditLinks.getInstance(mRedditClient);
//                mRedditAccount = new RedditAccount(mRedditClient);
//            }
//        });
    }

    public static RedditData getInstance(UserAgent userAgent, String refreshToken,
                                         String redditClientId, String redditRedirectUrl) {
        Log.v(LOG_TAG, "getInstance() called with: " + "userAgent = [" + userAgent + "]");

        if (sInstance == null) {
            sInstance = new RedditData(userAgent, refreshToken, redditClientId, redditRedirectUrl);
        }

        return sInstance;
    }

    public static RedditData newInstance(UserAgent userAgent, String refreshToken,
                                         String redditClientId, String redditRedirectUrl) {
        Log.v(LOG_TAG, "newInstance() called with: " + "userAgent = [" + userAgent + "]");
        sInstance = null;
        return getInstance(userAgent, refreshToken, redditClientId, redditRedirectUrl);
    }

    public void downvoteLink(final Link link, final RedditVoteCallback voteCallback) {
        Log.d(LOG_TAG,
                "downvoteLink() called with: " + "link = [" + link + "], voteCallback = [" + voteCallback + "]");
        verifyAuthentication(new RedditAuthenticationCallback() {
            @Override
            public void authenticationCallback(Exception e) {
                mRedditAccount.downvoteLink(link, voteCallback);
            }
        });
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

    public void getMoreLinks(final RedditLinksCallback linksCallback) {
        Log.v(LOG_TAG, "getMoreLinks()");
        verifyAuthentication(new RedditAuthenticationCallback() {
            @Override
            public void authenticationCallback(Exception e) {
                mRedditLinks.getMoreLinks(linksCallback);
            }
        });
    }

    public void getNewLinks(final SubredditRequest subredditRequest) {
        Log.v(LOG_TAG, "getNewLinks() called with: " + "subredditRequest = [" + subredditRequest + "]");
        verifyAuthentication(new RedditAuthenticationCallback() {
            @Override
            public void authenticationCallback(Exception e) {
                mRedditLinks.getNewLinks(subredditRequest);
            }
        });
    }

    public List<Link> getRedditLinksList() {
        return mRedditLinks.getRedditLinksList();
    }

    public void removeVote(final Link link, final RedditVoteCallback voteCallback) {
        Log.d(LOG_TAG,
                "removeVote() called with: " + "link = [" + link + "], voteCallback = [" + voteCallback + "]");
        verifyAuthentication(new RedditAuthenticationCallback() {
            @Override
            public void authenticationCallback(Exception e) {
                mRedditAccount.removeVote(link, voteCallback);
            }
        });
    }

    public void saveLink(final Link link, final RedditSaveCallback saveCallback) {
        Log.v(LOG_TAG, "saveLink() called with: " + "link = [" + link + "], saveCallback = [" + saveCallback + "]");
        mRedditAccount.saveLink(link, saveCallback);
    }

    public void unsaveLink(final Link link, final RedditSaveCallback saveCallback) {
        Log.v(LOG_TAG, "unsaveLink() called with: " + "link = [" + link + "], saveCallback = [" + saveCallback + "]");
        mRedditAccount.unsaveLink(link, saveCallback);
    }

    public void upvoteLink(final Link link, final RedditVoteCallback voteCallback) {
        Log.d(LOG_TAG,
                "upvoteLink() called with: " + "link = [" + link + "], voteCallback = [" + voteCallback + "]");
        verifyAuthentication(new RedditAuthenticationCallback() {
            @Override
            public void authenticationCallback(Exception e) {
                mRedditAccount.upvoteLink(link, voteCallback);
            }
        });
    }

    // TODO: This method needs to block before the requests are let through
    private synchronized void verifyAuthentication(
            RedditAuthenticationCallback authenticationCallback) {
        Log.d(LOG_TAG,
                "verifyAuthentication() is authenticated: " + mRedditClient.isAuthenticated());
        if (!mRedditClient.isAuthenticated()) {
            AuthenticationRequest authenticationRequest =
                    new AuthenticationRequest(mRedditClient, mRefreshToken, mRedditClientId,
                            mRedditRedirectUrl, authenticationCallback);
            AuthenticationTask authenticationTask = new AuthenticationTask();
            authenticationTask.execute(authenticationRequest);
        } else {
            // RedditClient is properly authenticated, run callback with no Exception
            authenticationCallback.authenticationCallback(null);
        }
    }


//    /**
//     * Updates the user's subreddit subscriptions and stores them in the app's preferences
//     *
//     * @param username The user whose subscriptions are being saved
//     */
//    private void updateSubreddits(String username) {
//        Log.v(LOG_TAG, "updateSubreddits() called with: " + "username = [" + username + "]");
//        UserSubredditsPaginator paginator = new UserSubredditsPaginator(mRedditClient,
//                "subscriber");
//
//        // A Set doesn't allow any duplicate insertions
//        HashSet<String> subredditSet = new HashSet<>();
//        while (paginator.hasNext()) {
//            Listing<Subreddit> subs = paginator.next(true);
//            if (subs != null && subs.getChildren() != null) {
//                for (Subreddit subreddit : subs.getChildren()) {
//                    subredditSet.add(subreddit.getDisplayName());
//                }
//            }
//        }
//
//        // Save the user's subscriptions for later use in the app
//        if (!subredditSet.isEmpty()) {
//            mRedditSubscriptions.setSubscriptions(username, subredditSet);
//            int count = mRedditSubscriptions.getSubscriptions(username).size();
//            Log.i(LOG_TAG, "Added " + count + " subscribed subreddits");
//            if (AppConfiguration.isDebuggable()) {
//                String logOutput = "";
//                for (String sub : subredditSet) {
//                    logOutput += sub + ", ";
//                }
//                Log.d(LOG_TAG, "subs: " + logOutput);
//            }
//        }
//    }
}
