package com.fernandobarillas.redditservice.results;

import net.dean.jraw.http.oauth.OAuthData;

/**
 * Created by fb on 8/3/16.
 */
public class OauthLoginResult {
    private String    mUsername;
    private String    mRefreshToken;
    private OAuthData mOAuthData;

    public OauthLoginResult(String username, String refreshToken, OAuthData OAuthData) {
        mUsername = username;
        mRefreshToken = refreshToken;
        mOAuthData = OAuthData;
    }

    @Override
    public String toString() {
        return "OauthLoginResult{" +
                "mUsername='" + mUsername + '\'' +
                ", mRefreshToken='" + mRefreshToken + '\'' +
                ", mOAuthData=" + mOAuthData +
                '}';
    }

    public OAuthData getOAuthData() {
        return mOAuthData;
    }

    public String getRefreshToken() {
        return mRefreshToken;
    }

    public String getUsername() {
        return mUsername;
    }
}
