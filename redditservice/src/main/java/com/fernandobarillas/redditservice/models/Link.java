package com.fernandobarillas.redditservice.models;

import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Thing;
import net.dean.jraw.models.VoteDirection;

import java.util.Date;

/**
 * Created by fb on 12/15/15.
 */
public class Link extends PublicContribution {
    private String mAuthor;
    private Boolean mIsNsfw;
    private Boolean mIsSaved;
    private String mDomain;
    private Double mUpvoteRatio;
    private String mPermaLink;
    private String mSubredditName;
    private String mThumbnail;
    private String mTitle;
    private String mUrl;
    private String mShortUrl;
    private Integer mScore;
    private Date mCreatedUtc;
    private VoteDirection mVoteDirection;
    private String mFullName;
    private String mId;

    public Link(Submission submission) {
        super(null);
        mAuthor = submission.getAuthor();
        mIsNsfw = submission.isNsfw();
        mIsSaved = submission.isSaved();
        mDomain = submission.getDomain();
        mUpvoteRatio = submission.getUpvoteRatio();
        mPermaLink = submission.getPermalink();
        mSubredditName = submission.getSubredditName();
        mThumbnail = submission.getThumbnail();
        mTitle = submission.getTitle();
        mUrl = submission.getUrl();
        mShortUrl = submission.getShortURL();
        mScore = submission.getScore();
        mCreatedUtc = submission.getCreatedUtc();
        mVoteDirection = submission.getVote();
        mFullName = submission.getFullName();
        mId = submission.getId();
    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }

        if (!(otherObject instanceof Thing)) {
            return false;
        }

        // Now that we know that the object we are checking is a Thing, cast it as one.
        Thing thing = (Thing) otherObject;

        // Compare the ID String values and use that as the return value
        return this.getId().equals(thing.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public String getAuthor() {
        return mAuthor;
    }

    public Date getCreatedUtc() {
        return mCreatedUtc;
    }

    public String getDomain() {
        return mDomain;
    }

    public String getId() {
        return mId;
    }

    public String getFullName() {
        return mFullName;
    }

    public String getPermalink() {
        return mPermaLink;
    }

    public Integer getScore() {
        return mScore;
    }

    public VoteDirection getVote() {
        return mVoteDirection;
    }

    public String getShortURL() {
        return mShortUrl;
    }

    public String getSubredditName() {
        return mSubredditName;
    }

    public String getThumbnail() {
        return mThumbnail;
    }

    public String getTitle() {
        return mTitle;
    }

    public Double getUpvoteRatio() {
        return mUpvoteRatio;
    }

    public String getUrl() {
        return mUrl;
    }

    public Boolean isNsfw() {
        return mIsNsfw;
    }

    public Boolean isSaved() {
        return mIsSaved;
    }
}
