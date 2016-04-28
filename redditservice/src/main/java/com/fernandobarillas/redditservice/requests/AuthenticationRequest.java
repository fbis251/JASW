package com.fernandobarillas.redditservice.requests;

import com.fernandobarillas.redditservice.callbacks.RedditAuthenticationCallback;

import net.dean.jraw.RedditClient;

/**
 * Created by fb on 12/15/15.
 */
public class AuthenticationRequest {
    public static final long INVALID_EXPIRATION_TIME = -1;

    private RedditClient                 mRedditClient;
    private String                       mRefreshToken;
    private String                       mRedditClientId;
    private String                       mRedditRedirectUrl;
    private String                       mAuthenticationJson;
    private long                         mExpirationTime;
    private RedditAuthenticationCallback mAuthenticationCallback;

    public AuthenticationRequest(RedditClient redditClient,
                                 String refreshToken,
                                 String redditClientId,
                                 String redditRedirectUrl,
                                 String authenticationJson,
                                 long expirationTime,
                                 RedditAuthenticationCallback authenticationCallback) {
        mRedditClient = redditClient;
        mRefreshToken = refreshToken;
        mRedditClientId = redditClientId;
        mRedditRedirectUrl = redditRedirectUrl;
        mAuthenticationJson = authenticationJson;
        mExpirationTime = expirationTime;
        mAuthenticationCallback = authenticationCallback;
    }

    public RedditAuthenticationCallback getAuthenticationCallback() {
        return mAuthenticationCallback;
    }

    public String getAuthenticationJson() {
        return mAuthenticationJson;
    }

    public long getExpirationTime() {
        return mExpirationTime;
    }

    public RedditClient getRedditClient() {
        return mRedditClient;
    }

    public String getRedditClientId() {
        return mRedditClientId;
    }

    public String getRedditRedirectUrl() {
        return mRedditRedirectUrl;
    }

    public String getRefreshToken() {
        return mRefreshToken;
    }

}
