package com.fernandobarillas.redditservice.requests;

import com.fernandobarillas.redditservice.callbacks.RedditSaveCallback;
import com.fernandobarillas.redditservice.models.Link;

import net.dean.jraw.managers.AccountManager;

/**
 * Created by fb on 12/15/15.
 */
public class SaveRequest {
    public static final boolean SAVE = true;
    public static final boolean UNSAVE = false;
    private Link mLink;
    private boolean mSave; // True to save, false to unsave
    private AccountManager mAccountManager;
    private RedditSaveCallback mRedditSaveCallback;

    public SaveRequest(Link link, boolean save, AccountManager accountManager, RedditSaveCallback redditSaveCallback) {
        mLink = link;
        mSave = save;
        mAccountManager = accountManager;
        mRedditSaveCallback = redditSaveCallback;
    }

    public Link getLink() {
        return mLink;
    }

    public boolean doSave() {
        return mSave;
    }

    public AccountManager getAccountManager() {
        return mAccountManager;
    }

    public RedditSaveCallback getRedditSaveCallback() {
        return mRedditSaveCallback;
    }
}
