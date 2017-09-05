package com.fernandobarillas.redditservice.utils;

import net.dean.jraw.http.oauth.OAuthData;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Utilities for dealing with {@link OAuthData} Objects
 */
public class OauthUtils {

    private static final Date INVALID_DATE = new Date(0);

    /**
     * The number of seconds the OAuth token will expire in, returned by the API during
     * authentication as part of the {@link OAuthData} Object
     */
    private static final String OAUTH_DATA_EXPIRES_IN = "expires_in";

    public static Date getExpirationDate(OAuthData oAuthData) {
        if (oAuthData == null) return INVALID_DATE;

        Date expirationDate = new Date();
        long expiresInSeconds = oAuthData.data(OAUTH_DATA_EXPIRES_IN, Long.class);
        long expiresInMilliseconds = TimeUnit.SECONDS.toMillis(expiresInSeconds);

        // Add the expires_in seconds to the current time
        expirationDate.setTime(expirationDate.getTime() + expiresInMilliseconds);
        return expirationDate;
    }
}
