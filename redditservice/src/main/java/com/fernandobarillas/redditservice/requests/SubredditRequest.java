package com.fernandobarillas.redditservice.requests;

import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

/**
 * Class that facilitates getting a request using JRAW's SubredditPaginator. This class uses a
 * builder pattern in order to avoid Exceptions with the request being changed after the Paginator
 * has been instantiated. Created by fb on 12/14/15.
 */
public class SubredditRequest extends SubmissionRequest {
    private final String mSubreddit;

    private SubredditRequest(Builder builder) {
        // Certain Sorting types don't support TimePeriods in the reddit API
        super(builder.subreddit,
                builder.after,
                builder.sorting,
                builder.sorting == Sorting.CONTROVERSIAL || builder.sorting == Sorting.TOP
                        ? builder.timePeriod : null,
                builder.linkLimit);
        mSubreddit = builder.subreddit;
    }

    @Override
    public String toString() {
        return "SubredditRequest{"
                + "subreddit='"
                + mSubreddit
                + '\''
                + ", after='"
                + mAfter
                + '\''
                + ", mSorting="
                + mSorting
                + ", mTimePeriod="
                + mTimePeriod
                + ", mLinkLimit="
                + mLinkLimit
                + '}';
    }

    public String getSubreddit() {
        return mSubreddit;
    }

    public static class Builder {
        // Required parameters
        private final String subreddit;

        // Optional parameters, using default values
        private String     after      = null;
        private Sorting    sorting    = Sorting.HOT;
        private TimePeriod timePeriod = null;
        private int        linkLimit  = 100;

        public Builder(String subreddit) {
            this.subreddit = subreddit;
        }

        public SubredditRequest build() {
            return new SubredditRequest(this);
        }

        public Builder setAfter(String after) {
            this.after = after;
            return this;
        }

        public Builder setLinkLimit(int linkLimit) {
            this.linkLimit = linkLimit;
            return this;
        }

        public Builder setSorting(Sorting sorting) {
            this.sorting = sorting;
            return this;
        }

        public Builder setTimePeriod(TimePeriod timePeriod) {
            this.timePeriod = timePeriod;
            return this;
        }
    }
}
