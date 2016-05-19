package com.fernandobarillas.redditservice.results;

/**
 * Created by fb on 5/1/16.
 */
public class AuthResult {
    private boolean mIsAuthenticated;
    private String  mAuthenticationJson;
    private long    mExpirationTime;

    public AuthResult(boolean isAuthenticated, String authenticationJson, long expirationTime) {
        mIsAuthenticated = isAuthenticated;
        mAuthenticationJson = authenticationJson;
        mExpirationTime = expirationTime;
    }

    public String getAuthenticationJson() {
        return mAuthenticationJson;
    }

    public long getExpirationTime() {
        return mExpirationTime;
    }

    public boolean isAuthenticated() {
        return mIsAuthenticated;
    }
}
