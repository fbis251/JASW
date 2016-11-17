package com.fernandobarillas.redditservice.requests;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.dean.jraw.http.UserAgent;

/**
 * Created by fb on 7/27/16.
 */
public class StartServiceRequest {
    String    mUsername;
    String    mClientId;
    String    mRedirectUri;
    UserAgent mAppUserAgent;

    /**
     * Builds a request to start the {@link com.fernandobarillas.redditservice.RedditService} using
     * the parameters needed when binding to the RedditService
     *
     * @param username     The reddit username to use during authentication. Can be null to start a
     *                     user-less session (guest mode)
     * @param clientId     The installed-application client ID
     * @param redirectUri  The redirect URL for the application
     * @param appUserAgent The user agent to send with all requests made to the reddit API
     */
    public StartServiceRequest(
            @Nullable String username,
            @NonNull String clientId,
            @NonNull String redirectUri,
            @NonNull UserAgent appUserAgent) {
        mUsername = username;
        mClientId = clientId;
        mRedirectUri = redirectUri;
        mAppUserAgent = appUserAgent;
    }

    @Override
    public String toString() {
        return "StartServiceRequest{" +
                "mUsername='" + mUsername + '\'' +
                ", mClientId='" + mClientId + '\'' +
                ", mRedirectUri='" + mRedirectUri + '\'' +
                ", mAppUserAgent=" + mAppUserAgent +
                '}';
    }

    public UserAgent getAppUserAgent() {
        return mAppUserAgent;
    }

    public String getClientId() {
        return mClientId;
    }

    public String getRedirectUri() {
        return mRedirectUri;
    }

    public String getUsername() {
        return mUsername;
    }
}
