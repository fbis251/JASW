package com.fernandobarillas.redditservice.callbacks;

/**
 * Created by fb on 12/15/15.
 */
public interface RedditAuthenticationCallback {
    void authenticationCallback(String authenticationJson, long expirationTime, Exception e);
}
