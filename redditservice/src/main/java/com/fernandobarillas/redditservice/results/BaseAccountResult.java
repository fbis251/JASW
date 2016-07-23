package com.fernandobarillas.redditservice.results;

import net.dean.jraw.models.PublicContribution;

/**
 * Created by fb on 5/18/16.
 */
public abstract class BaseAccountResult {
    private PublicContribution mLink;
    private boolean            mSuccessful;
    private Throwable          mThrowable;

    public BaseAccountResult(PublicContribution link) {
        mLink = link;
    }

    public PublicContribution getLink() {
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
