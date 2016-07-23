package com.fernandobarillas.redditservice.results;

/**
 * Created by fb on 5/1/16.
 */
public class AuthResult {
    private boolean mIsAuthenticated;
    private String  mAuthenticationJson;
    private long    mExpirationTime;
    private boolean isCachedData;

    public AuthResult(boolean isAuthenticated, String authenticationJson, long expirationTime,
            boolean isCachedData) {
        mIsAuthenticated = isAuthenticated;
        mAuthenticationJson = authenticationJson;
        mExpirationTime = expirationTime;
        this.isCachedData = isCachedData;
    }

    @Override
    public String toString() {
        return "AuthResult{" +
                "mIsAuthenticated=" + mIsAuthenticated +
                ", mAuthenticationJson='" + mAuthenticationJson + '\'' +
                ", mExpirationTime=" + mExpirationTime +
                ", isCachedData=" + isCachedData +
                '}';
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

    public boolean isCachedData() {
        return isCachedData;
    }
}
