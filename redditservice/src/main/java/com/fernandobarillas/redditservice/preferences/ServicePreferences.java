package com.fernandobarillas.redditservice.preferences;

import android.content.Context;

public class ServicePreferences extends BasePreferences {
    private static final String PREFERENCES_NAME     = "reddit_service_preferences";
    private static final String REDIRECT_URI_KEY     = "redirect_uri";
    private static final String REDDIT_CLIENT_ID_KEY = "reddit_client_id";

    public ServicePreferences(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public String getRedditClientId() {
        return mSharedPreferences.getString(REDDIT_CLIENT_ID_KEY, null);
    }

    public void setRedditClientId(String redditClientId) {
        setStringPreference(REDDIT_CLIENT_ID_KEY, redditClientId);
    }

    public String getRedditRedirectUrl() {
        return mSharedPreferences.getString(REDIRECT_URI_KEY, null);
    }

    public void setRedditRedirectUri(String redditRedirectUrl) {
        setStringPreference(REDIRECT_URI_KEY, redditRedirectUrl);
    }
}
