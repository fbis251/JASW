package com.fernandobarillas.redditservice.requests;

import com.fernandobarillas.redditservice.callbacks.RedditAuthenticationCallback;

import net.dean.jraw.RedditClient;

/**
 * Created by fb on 12/15/15.
 */
public class AuthenticationRequest {
    private RedditClient mRedditClient;
    private String mRefreshToken;
    private String mRedditClientId;
    private String mRedditRedirectUrl;
    private RedditAuthenticationCallback mAuthenticationCallback;

    public AuthenticationRequest(RedditClient redditClient, String refreshToken,
                                 String redditClientId, String redditRedirectUrl,
                                 RedditAuthenticationCallback authenticationCallback) {
        mRedditClient = redditClient;
        mRefreshToken = refreshToken;
        mRedditClientId = redditClientId;
        mRedditRedirectUrl = redditRedirectUrl;
        mAuthenticationCallback = authenticationCallback;
    }

    public RedditAuthenticationCallback getAuthenticationCallback() {
        return mAuthenticationCallback;
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
