package com.fernandobarillas.redditservice.preferences;

import android.content.Context;

public class ServicePreferences extends BasePreferences {
    private static final String PREFERENCES_NAME     = "reddit_service_preferences";
    private static final String REDIRECT_URL_KEY     = "redirect_url";
    private static final String USER_AGENT_KEY       = "user_agent";
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
        return mSharedPreferences.getString(REDIRECT_URL_KEY, null);
    }

    public void setRedditRedirectUrl(String redditRedirectUrl) {
        setStringPreference(REDIRECT_URL_KEY, redditRedirectUrl);
    }

    public String getUserAgentString() {
        return mSharedPreferences.getString(USER_AGENT_KEY, null);
    }

    public void setUserAgentString(String userAgentString) {
        setStringPreference(USER_AGENT_KEY, userAgentString);
    }
}
