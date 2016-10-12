package com.fernandobarillas.redditservice.requests;

import net.dean.jraw.http.oauth.Credentials;

/**
 * Created by fb on 12/18/15.
 */
public class OauthLoginRequest {
    private Credentials  mCredentials;
    private String       mAuthorizationUrl;

    public OauthLoginRequest(Credentials credentials, String authorizationUrl) {
        mCredentials = credentials;
        mAuthorizationUrl = authorizationUrl;
    }

    @Override
    public String toString() {
        return "OauthLoginRequest{" +
                ", mCredentials=" + mCredentials +
                ", mAuthorizationUrl='" + mAuthorizationUrl + '\'' +
                '}';
    }

    public String getAuthorizationUrl() {
        return mAuthorizationUrl;
    }

    public Credentials getCredentials() {
        return mCredentials;
    }
}
