package com.fernandobarillas.redditservice.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.fernandobarillas.redditservice.requests.AuthRequest;

/**
 * Created by fb on 5/18/16.
 */
public class RedditAuthPreferences extends BasePreferences {
    private static final String PREFERENCES_PREFIX      = "reddit_auth_";
    private static final String PREFERENCES_USERLESS    = "reddit_userless_auth";
    private static final String AUTHENTICATION_JSON_KEY = "authentication_json";
    private static final String EXPIRATION_TIME_KEY     = "expiration_time";
    private static final String REFRESH_TOKEN_KEY       = "refresh_token";
    private static final String USERNAME_KEY            = "username";

    public RedditAuthPreferences(Context context, String username) {
        String preferencesName = PREFERENCES_USERLESS;
        if (!TextUtils.isEmpty(username)) {
            preferencesName = PREFERENCES_PREFIX + username;
        }
        mSharedPreferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        setStringPreference(USERNAME_KEY, username);
    }

    /**
     * Immediately commits pending operations. Useful when you need to make sure important values
     * such as authentication data are committed right away.
     */
    @SuppressLint("ApplySharedPref")
    public void commit() {
        mSharedPreferences.edit().commit();
    }

    /**
     * Deletes all preferences stored for this reddit user, useful when doing a revoke/logout
     */
    @SuppressLint("ApplySharedPref")
    public void delete() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor
                .remove(AUTHENTICATION_JSON_KEY)
                .remove(EXPIRATION_TIME_KEY)
                .remove(REFRESH_TOKEN_KEY)
                .remove(USERNAME_KEY)
                .clear()
                .apply();
    }

    public String getAuthenticationJson() {
        return mSharedPreferences.getString(AUTHENTICATION_JSON_KEY, null);
    }

    public long getExpirationTime() {
        return mSharedPreferences.getLong(EXPIRATION_TIME_KEY, AuthRequest.INVALID_EXPIRATION_TIME);
    }

    public String getRefreshToken() {
        return mSharedPreferences.getString(REFRESH_TOKEN_KEY, null);
    }

    public String getUsername() {
        return mSharedPreferences.getString(USERNAME_KEY, null);
    }

    public void setAuthenticationJson(String authenticationJson) {
        setStringPreference(AUTHENTICATION_JSON_KEY, authenticationJson);
    }

    public void setExpirationTime(long expirationTime) {
        setLongPreference(EXPIRATION_TIME_KEY, expirationTime);
    }

    public void setRefreshToken(String refreshToken) {
        setStringPreference(REFRESH_TOKEN_KEY, refreshToken);
    }
}
