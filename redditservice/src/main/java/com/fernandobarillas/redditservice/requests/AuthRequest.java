package com.fernandobarillas.redditservice.requests;

/**
 * Created by fb on 12/15/15.
 */
public class AuthRequest {
    public static final long INVALID_EXPIRATION_TIME = -1;

    private String mRefreshToken;
    private String mRedditClientId;
    private String mRedditRedirectUri;
    private String mAuthenticationJson;
    private long   mExpirationTime;

    public AuthRequest(String refreshToken, String redditClientId, String redditRedirectUri,
            String authenticationJson, long expirationTime) {
        mRefreshToken = refreshToken;
        mRedditClientId = redditClientId;
        mRedditRedirectUri = redditRedirectUri;
        mAuthenticationJson = authenticationJson;
        mExpirationTime = expirationTime;
    }

    @Override
    public String toString() {
        return "AuthRequest{" +
                "mRefreshToken='" + mRefreshToken + '\'' +
                ", mRedditClientId='" + mRedditClientId + '\'' +
                ", mRedditRedirectUri='" + mRedditRedirectUri + '\'' +
                ", mAuthenticationJson='" + mAuthenticationJson + '\'' +
                ", mExpirationTime=" + mExpirationTime +
                '}';
    }

    public String getAuthenticationJson() {
        return mAuthenticationJson;
    }

    public long getExpirationTime() {
        return mExpirationTime;
    }

    public String getRedditClientId() {
        return mRedditClientId;
    }

    public String getRedditRedirectUri() {
        return mRedditRedirectUri;
    }

    public String getRefreshToken() {
        return mRefreshToken;
    }
}
