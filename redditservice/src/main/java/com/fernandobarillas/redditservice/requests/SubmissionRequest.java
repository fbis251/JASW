package com.fernandobarillas.redditservice.requests;

import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

/**
 * Created by fb on 10/13/16.
 */

public abstract class SubmissionRequest {

    public static final int REQUEST_SUBREDDIT        = 0;
    public static final int REQUEST_DOMAIN           = 1;
    public static final int REQUEST_USER_SUBMISSIONS = 2;
    // TODO: 10/13/16 add some common methods here
    protected final String     mRequestName;
    protected final long       mRequestId;
    protected final Sorting    mSorting;
    protected final TimePeriod mTimePeriod;
    protected final int        mLinkLimit;

    public int getLinkLimit() {
        return mLinkLimit;
    }

    public long getRequestId() {
        return mRequestId;
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

    protected SubmissionRequest(
            String requestName,
            long requestId,
            Sorting sorting,
            TimePeriod timePeriod,
            int linkLimit) {
        mRequestName = requestName;
        mRequestId = requestId;
        mSorting = sorting;
        mTimePeriod = timePeriod;
        mLinkLimit = linkLimit;
    }
}
