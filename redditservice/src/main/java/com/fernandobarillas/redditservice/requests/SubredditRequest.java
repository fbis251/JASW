package com.fernandobarillas.redditservice.requests;

import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

/**
 * Class that facilitates getting a request using JRAW's SubredditPaginator. This class uses a
 * builder pattern in order to avoid Exceptions with the request being changed after the Paginator
 * has been instantiated. Created by fb on 12/14/15.
 */
public class SubredditRequest {
    private final String     mSubreddit;
    private final Sorting    mSorting;
    private final TimePeriod mTimePeriod;
    private final int        mLinkLimit;

    private SubredditRequest(Builder builder) {
        mSubreddit = builder.subreddit;
        mSorting = builder.mSorting;
        mLinkLimit = builder.mLinkLimit;

        // Certain Sorting types don't support TimePeriods in the reddit API
        if (mSorting == Sorting.CONTROVERSIAL || mSorting == Sorting.TOP) {
            mTimePeriod = builder.mTimePeriod;
        } else {
            mTimePeriod = null;
        }
    }

    @Override
    public String toString() {
        return "SubredditRequest{" +
                "mSubreddit='" + mSubreddit + '\'' +
                ", mSorting=" + mSorting +
                ", mTimePeriod=" + mTimePeriod +
                ", mLinkLimit=" + mLinkLimit +
                '}';
    }

    public int getLinkLimit() {
        return mLinkLimit;
    }

    public Sorting getSorting() {
        return mSorting;
    }

    public String getSubreddit() {
        return mSubreddit;
    }

    public TimePeriod getTimePeriod() {
        return mTimePeriod;
    }

    public static class Builder {
        // Required parameters
        private final String subreddit;

        // Optional parameters, using default values
        private Sorting    mSorting    = Sorting.HOT;
        private TimePeriod mTimePeriod = TimePeriod.DAY;
        private int        mLinkLimit  = 100;

        public Builder(String subreddit) {
            this.subreddit = subreddit;
        }

        public SubredditRequest build() {
            return new SubredditRequest(this);
        }

        public Builder setLinkLimit(int linkLimit) {
            mLinkLimit = linkLimit;
            return this;
        }

        public Builder setSorting(Sorting sorting) {
            mSorting = sorting;
            return this;
        }

        public Builder setTimePeriod(TimePeriod timePeriod) {
            mTimePeriod = timePeriod;
            return this;
        }
    }
}
