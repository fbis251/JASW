package com.fernandobarillas.redditservice.data;

import android.text.TextUtils;
import android.util.Log;

import com.fernandobarillas.redditservice.callbacks.RedditAuthenticationCallback;
import com.fernandobarillas.redditservice.callbacks.RedditLinksCallback;
import com.fernandobarillas.redditservice.callbacks.RedditSaveCallback;
import com.fernandobarillas.redditservice.callbacks.RedditVoteCallback;
import com.fernandobarillas.redditservice.models.Link;
import com.fernandobarillas.redditservice.observables.UserSubredditsObservable;
import com.fernandobarillas.redditservice.requests.AuthenticationRequest;
import com.fernandobarillas.redditservice.requests.SubredditRequest;
import com.fernandobarillas.redditservice.tasks.AuthenticationTask;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.LoggingMode;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.HashSet;
import java.util.List;

import rx.Observable;

/**
 * Created by fb on 12/14/15.
 */
public class RedditData {
    public static final  int    DOWNLOAD_RETRIES = 3; // Download attempts before giving up
    private static final String LOG_TAG          = "RedditData";
    private static RedditData    sInstance;
    private        RedditLinks   mRedditLinks;
    private        RedditAccount mRedditAccount;
    private        RedditClient  mRedditClient;
    private        String        mAuthenticationJson;
    private        long          mExpirationTime;

    private UserAgent mUserAgent;
    private String    mRefreshToken;
    private String    mRedditClientId;
    private String    mRedditRedirectUrl;
    private boolean   mNeedsAuthentication;

    private RedditData(UserAgent userAgent,
                       String refreshToken,
                       String redditClientId,
                       String redditRedirectUrl,
                       String authenticationJson,
                       long expirationTime) {
        Log.d(LOG_TAG, "RedditData()");

        if (TextUtils.isEmpty(redditClientId)) {
            Log.e(LOG_TAG, "RedditData: redditClientId is empty!");
        }

        mNeedsAuthentication = true;
        mUserAgent = userAgent;
        mRefreshToken = refreshToken;
        mRedditClientId = redditClientId;
        mRedditRedirectUrl = redditRedirectUrl;
        mAuthenticationJson = authenticationJson;
        mExpirationTime = expirationTime;

        mRedditClient = new RedditClient(mUserAgent);
        // TODO: Handle setting logging mode when app isn't debuggable
//        mRedditClient.setLoggingMode(LoggingMode.ALWAYS);
        mRedditClient.setLoggingMode(LoggingMode.ON_FAIL);
        mRedditClient.setRetryLimit(DOWNLOAD_RETRIES);

        mRedditLinks = new RedditLinks(mRedditClient);
        mRedditAccount = new RedditAccount(mRedditClient);
    }

    public static RedditData getInstance(UserAgent userAgent,
                                         String refreshToken,
                                         String redditClientId,
                                         String redditRedirectUrl,
                                         String authenticationJson,
                                         long expirationTime) {
        Log.v(LOG_TAG, "getInstance() called with: " + "userAgent = [" + userAgent + "]");

        if (sInstance == null) {
            sInstance = new RedditData(userAgent,
                                       refreshToken,
                                       redditClientId,
                                       redditRedirectUrl,
                                       authenticationJson,
                                       expirationTime);
        }

        return sInstance;
    }

    public static RedditData newInstance(UserAgent userAgent,
                                         String refreshToken,
                                         String redditClientId,
                                         String redditRedirectUrl,
                                         String authenticationJson,
                                         long expirationTime) {
        Log.v(LOG_TAG, "newInstance() called with: " + "userAgent = [" + userAgent + "]");
        sInstance = null;
        return getInstance(userAgent,
                           refreshToken,
                           redditClientId,
                           redditRedirectUrl,
                           authenticationJson,
                           expirationTime);
    }

    public void downvoteLink(final Link link, final RedditVoteCallback voteCallback) {
        Log.d(LOG_TAG, "downvoteLink() called with: " + "link = [" + link + "], voteCallback = [" + voteCallback + "]");
        verifyAuthentication(new RedditAuthenticationCallback() {
            @Override
            public void authenticationCallback(String authenticationJson, long expirationTime, Exception e) {
                if (e == null) {
                    mRedditAccount.downvoteLink(link, voteCallback);
                } else if (voteCallback != null) {
                    voteCallback.voteCallback(e);
                } else {
                    Log.e(LOG_TAG, "authenticationCallback: ", e);
                }
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

    public int getNsfwImageCount() {
        return mRedditLinks.getNsfwImageCount();
    }

    public void getMoreLinks(final RedditLinksCallback linksCallback) {
        Log.v(LOG_TAG, "getMoreLinks()");
        verifyAuthentication(new RedditAuthenticationCallback() {
            @Override
            public void authenticationCallback(String authenticationJson, long expirationTime, Exception e) {
                if (e == null) {
                    mRedditLinks.getMoreLinks(linksCallback);
                } else if (linksCallback != null) {
                    linksCallback.linksCallback(e);
                } else {
                    Log.e(LOG_TAG, "authenticationCallback: ", e);
                }
            }
        });
    }

    public void getNewLinks(final SubredditRequest subredditRequest) {
        Log.v(LOG_TAG, "getNewLinks() called with: " + "subredditRequest = [" + subredditRequest + "]");

        verifyAuthentication(new RedditAuthenticationCallback() {
            @Override
            public void authenticationCallback(String authenticationJson, long expirationTime, Exception e) {
                if (e != null) {
                    if (subredditRequest.getRedditLinksCallback() != null) {
                        subredditRequest.getRedditLinksCallback().linksCallback(e);
                    }
                    return;
                }
                int                       attempts         = 0;
                final RedditLinksCallback originalCallback = subredditRequest.getRedditLinksCallback();
                RedditLinksCallback newCallback = new RedditLinksCallback() {
                    @Override
                    public void linksCallback(Exception e) {
                        if (e != null) {
                            if (e instanceof NetworkException) {
                                NetworkException networkException = (NetworkException) e;
                                Log.e(LOG_TAG,
                                      "linksCallback: Error code: " + ((NetworkException) e).getResponse()
                                              .getStatusCode());
                                Log.e(LOG_TAG, "linksCallback: ", e);
                                mNeedsAuthentication = true;
                            } else {
                                Log.e(LOG_TAG, "linksCallback: THE EXCEPTION IS", e);
                            }
                        }
                        if (originalCallback != null) {
                            originalCallback.linksCallback(e);
                        }
                    }
                };

                subredditRequest.setRedditLinksCallback(newCallback);

                while (attempts++ < DOWNLOAD_RETRIES) {
                    mRedditLinks.getNewLinks(subredditRequest);
                }
            }
        });
    }

    public List<Link> getRedditLinksList() {
        return mRedditLinks.getRedditLinksList();
    }

    public void removeVote(final Link link, final RedditVoteCallback voteCallback) {
        Log.d(LOG_TAG, "removeVote() called with: " + "link = [" + link + "], voteCallback = [" + voteCallback + "]");
        verifyAuthentication(new RedditAuthenticationCallback() {
            @Override
            public void authenticationCallback(String authenticationJson, long expirationTime, Exception e) {
                if (e == null) {
                    mRedditAccount.removeVote(link, voteCallback);
                } else if (voteCallback != null) {
                    voteCallback.voteCallback(e);
                } else {
                    Log.e(LOG_TAG, "authenticationCallback: ", e);
                }
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
        Log.d(LOG_TAG, "upvoteLink() called with: " + "link = [" + link + "], voteCallback = [" + voteCallback + "]");
        verifyAuthentication(new RedditAuthenticationCallback() {
            @Override
            public void authenticationCallback(String authenticationJson, long expirationTime, Exception e) {
                if (e == null) {
                    mRedditAccount.upvoteLink(link, voteCallback);
                } else if (voteCallback != null) {
                    voteCallback.voteCallback(e);
                } else {
                    Log.e(LOG_TAG, "authenticationCallback: ", e);
                }
            }
        });
    }

    public void voteLink(final Link link, final VoteDirection voteDirection, final RedditVoteCallback voteCallback) {
        Log.v(LOG_TAG, "voteLink() called with: " + "link = [" + link +
                "], voteDirection = [" + voteDirection +
                "], voteCallback = [" + voteCallback + "]");
        verifyAuthentication(new RedditAuthenticationCallback() {
            @Override
            public void authenticationCallback(String authenticationJson, long expirationTime, Exception e) {
                if (e != null) {
                    if (voteCallback != null) voteCallback.voteCallback(e);
                    Log.e(LOG_TAG, "authenticationCallback: ", e);
                    return;
                }

                mRedditAccount.voteLink(link, voteDirection, voteCallback);
            }
        });
    }

    // TODO: This method needs to block before the requests are let through
    public synchronized void verifyAuthentication(RedditAuthenticationCallback authenticationCallback) {
        Log.v(LOG_TAG, "verifyAuthentication() is authenticated: " + mRedditClient.isAuthenticated());
        if (mNeedsAuthentication || !mRedditClient.isAuthenticated()) {
            Log.v(LOG_TAG, "verifyAuthentication: Client needs authentication, running auth task");
            AuthenticationRequest authenticationRequest = new AuthenticationRequest(mRedditClient,
                                                                                    mRefreshToken,
                                                                                    mRedditClientId,
                                                                                    mRedditRedirectUrl,
                                                                                    mAuthenticationJson,
                                                                                    mExpirationTime,
                                                                                    authenticationCallback);
            AuthenticationTask authenticationTask = new AuthenticationTask();
            authenticationTask.execute(authenticationRequest);
            mNeedsAuthentication = false;
        } else {
            // RedditClient is properly authenticated, run onComplete with no Exception
            authenticationCallback.authenticationCallback(mAuthenticationJson, mExpirationTime, null);
        }
    }

    public Observable<List<Subreddit>> getUserSubredditsObservable() {
        UserSubredditsObservable subredditsObservable = new UserSubredditsObservable(mRedditClient);
        return subredditsObservable.getObservable();
    }

    /**
     * Gets the logged in user's reddit subreddit subscriptions
     */
    public HashSet<String> getUserSubreddits() throws NetworkException, IllegalStateException {
        Log.v(LOG_TAG, "getObservable() called");
        UserSubredditsPaginator paginator = new UserSubredditsPaginator(mRedditClient, "subscriber");

        // A Set doesn't allow any duplicate insertions
        HashSet<String> subredditSet = new HashSet<>();
        while (paginator.hasNext()) {
            Listing<Subreddit> subs = paginator.next(true);
            if (subs != null && subs.getChildren() != null) {
                for (Subreddit subreddit : subs.getChildren()) {
                    subredditSet.add(subreddit.getDisplayName());
                }
            }
        }

        return subredditSet;
    }
}
