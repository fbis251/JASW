package com.fernandobarillas.redditservice.results;

/**
 * Created by fb on 5/1/16.
 */
public class AuthResult {
    private boolean mIsAuthenticated;
    private String  mUsername;
    private String  mAuthenticationJson;
    private long    mExpirationTime;

    public AuthResult(boolean isAuthenticated,
                      String username,
                      String authenticationJson,
                      long expirationTime) {
        mIsAuthenticated = isAuthenticated;
        mUsername = username;
        mAuthenticationJson = authenticationJson;
        mExpirationTime = expirationTime;
    }

    public boolean isAuthenticated() {
        return mIsAuthenticated;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getAuthenticationJson() {
        return mAuthenticationJson;
    }

    public long getExpirationTime() {
        return mExpirationTime;
    }
}
