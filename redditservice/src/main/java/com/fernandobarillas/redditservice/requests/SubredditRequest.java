package com.fernandobarillas.redditservice.requests;

import com.fernandobarillas.redditservice.data.RedditLinks;

import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

/**
 * Created by fb on 12/14/15.
 */
public class SubredditRequest {
    private String mSubreddit;
    private Sorting mSorting;
    private TimePeriod mTimePeriod;
    private int mLinkLimit;
    private boolean mShowNsfw;

    public SubredditRequest(String subreddit) {
        mSubreddit = subreddit;
        mSorting = Sorting.HOT;
        mTimePeriod = TimePeriod.DAY;
        mLinkLimit = RedditLinks.LINK_LIMIT;
        mShowNsfw = false;
    }

    public String getSubreddit() {
        return mSubreddit;
    }

    public SubredditRequest setSubreddit(String subreddit) {
        mSubreddit = subreddit;
        return this;
    }

    public Sorting getSorting() {
        return mSorting;
    }

    public SubredditRequest setSorting(Sorting sorting) {
        mSorting = sorting;
        return this;
    }

    public TimePeriod getTimePeriod() {
        return mTimePeriod;
    }

    public SubredditRequest setTimePeriod(TimePeriod timePeriod) {
        mTimePeriod = timePeriod;
        return this;
    }

    public int getLinkLimit() {
        return mLinkLimit;
    }

    public SubredditRequest setLinkLimit(int linkLimit) {
        mLinkLimit = linkLimit;
        return this;
    }

    public boolean isShowNsfwEnabled() {
        return mShowNsfw;
    }

    public SubredditRequest setShowNsfw(boolean showNsfw) {
        mShowNsfw = showNsfw;
        return this;
    }
}
