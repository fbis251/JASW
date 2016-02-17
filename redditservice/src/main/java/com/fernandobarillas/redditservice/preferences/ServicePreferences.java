package com.fernandobarillas.redditservice.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class ServicePreferences {
    public static final String PREFERENCES_KEY = "REDDIT_SERVICE_PREFERENCES";
    private static final String LOG_TAG = "ServicePreferences";
    private static final String REDIRECT_URL_KEY = "redirect_url";
    private static final String USER_AGENT_KEY = "user_agent";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";
    private static final String REDDIT_CLIENT_ID_KEY = "reddit_client_id";
    private SharedPreferences mSharedPreferences;

    public ServicePreferences(Context context) {
        Log.v(LOG_TAG, "ServicePreferences() called with: " + "context = [" + context + "]");
        Log.d("ServicePreferences", "KEY " + PREFERENCES_KEY);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getRedditRedirectUrl() {
        return mSharedPreferences.getString(REDIRECT_URL_KEY, null);
    }

    public void setRedditRedirectUrl(String redditRedirectUrl) {
        setStringPreference(REDIRECT_URL_KEY, redditRedirectUrl);
    }

    public String getRedditClientId() {
        return mSharedPreferences.getString(REDDIT_CLIENT_ID_KEY, null);
    }

    public void setRedditClientId(String redditClientId) {
        setStringPreference(REDDIT_CLIENT_ID_KEY, redditClientId);
    }

    public String getRefreshToken() {
        return mSharedPreferences.getString(REFRESH_TOKEN_KEY, null);
    }

    public void setRefreshToken(String refreshToken) {
        setStringPreference(REFRESH_TOKEN_KEY, refreshToken);
    }

    public String getUserAgentString() {
        return mSharedPreferences.getString(USER_AGENT_KEY, null);
    }

    public void setUserAgentString(String userAgentString) {
        setStringPreference(USER_AGENT_KEY, userAgentString);
    }

    private void setStringPreference(String key, String value) {
        mSharedPreferences.edit().putString(key, value).apply();
    }
}
