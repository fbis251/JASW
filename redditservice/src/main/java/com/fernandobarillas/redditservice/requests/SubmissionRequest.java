package com.fernandobarillas.redditservice.requests;

import android.support.annotation.Nullable;

import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

/**
 * Created by fb on 10/13/16.
 */

public abstract class SubmissionRequest {

    protected final String     mRequestName;
    protected final String     mAfter;
    protected final Sorting    mSorting;
    protected final TimePeriod mTimePeriod;
    protected final int        mLinkLimit;

    public int getLinkLimit() {
        return mLinkLimit;
    }

    public String getAfter() {
        return mAfter;
    }

    public String getRequestName() {
        return mRequestName;
    }

    public Sorting getSorting() {
        return mSorting;
    }

    public TimePeriod getTimePeriod() {
        return mTimePeriod;
    }

    protected SubmissionRequest(String requestName,
            @Nullable String after,
            Sorting sorting,
            TimePeriod timePeriod,
            int linkLimit) {
        mRequestName = requestName;
        mAfter = after;
        mSorting = sorting;
        mTimePeriod = timePeriod;
        mLinkLimit = linkLimit;
    }
}
