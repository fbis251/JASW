package com.fernandobarillas.redditservice.preferences;

import android.content.SharedPreferences;

/**
 * Created by fb on 5/18/16.
 */
public class BasePreferences {
    protected SharedPreferences mSharedPreferences;

    protected void setLongPreference(String key, long value) {
        mSharedPreferences.edit().putLong(key, value).apply();
    }

    protected void setStringPreference(String key, String value) {
        mSharedPreferences.edit().putString(key, value).apply();
    }
}
