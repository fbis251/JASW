package com.fernandobarillas.redditservice.requests;

import android.support.annotation.NonNull;

import com.fernandobarillas.redditservice.callbacks.RedditLinksCallback;
import com.fernandobarillas.redditservice.links.validators.LinkValidator;

import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

/**
 * Created by fb on 12/14/15.
 */
public class SubredditRequest {
    private final String mSubreddit;
    private final Sorting mSorting;
    private final TimePeriod mTimePeriod;
    private final int mLinkLimit;
    private final LinkValidator mLinkValidator;
    // Only the callback should be dynamically changed since it's the only field nto passed in to
    // the JRAW Paginator which will avoid getting an exception when the Paginator state changes
    private RedditLinksCallback mRedditLinksCallback;

    private SubredditRequest(Builder builder) {
        mSubreddit = builder.subreddit;
        mSorting = builder.mSorting;
        mTimePeriod = builder.mTimePeriod;
        mLinkLimit = builder.mLinkLimit;
        mLinkValidator = builder.mLinkValidator;
        mRedditLinksCallback = builder.mRedditLinksCallback;
    }

    public int getLinkLimit() {
        return mLinkLimit;
    }

    public LinkValidator getLinkValidator() {
        return mLinkValidator;
    }

    public RedditLinksCallback getRedditLinksCallback() {
        return mRedditLinksCallback;
    }

    public void setRedditLinksCallback(RedditLinksCallback linksCallback) {
        mRedditLinksCallback = linksCallback;
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
        private Sorting mSorting = Sorting.HOT;
        private TimePeriod mTimePeriod = TimePeriod.DAY;
        private int mLinkLimit = 100;
        private LinkValidator mLinkValidator = null;
        private RedditLinksCallback mRedditLinksCallback = null;

        public Builder(@NonNull String subreddit) {
            this.subreddit = subreddit;
        }

        public SubredditRequest build() {
            return new SubredditRequest(this);
        }

        public Builder setLinkLimit(int linkLimit) {
            mLinkLimit = linkLimit;
            return this;
        }

        public Builder setLinkValidator(LinkValidator linkValidator) {
            mLinkValidator = linkValidator;
            return this;
        }

        public Builder setRedditLinksCallback(RedditLinksCallback redditLinksCallback) {
            mRedditLinksCallback = redditLinksCallback;
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
