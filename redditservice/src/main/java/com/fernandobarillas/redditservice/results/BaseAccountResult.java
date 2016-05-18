package com.fernandobarillas.redditservice.results;

import com.fernandobarillas.redditservice.models.Link;

/**
 * Created by fb on 5/18/16.
 */
public abstract class BaseAccountResult {
    private Link      mLink;
    private boolean   mSuccessful;
    private Throwable mThrowable;

    public BaseAccountResult(Link link) {
        mLink = link;
    }

    public Link getLink() {
        return mLink;
    }

    public Throwable getThrowable() {
        return mThrowable;
    }

    public void setThrowable(Throwable throwable) {
        mThrowable = throwable;
    }

    public boolean isSuccessful() {
        return mSuccessful;
    }

    public void setSuccessful(boolean successful) {
        mSuccessful = successful;
    }
}
