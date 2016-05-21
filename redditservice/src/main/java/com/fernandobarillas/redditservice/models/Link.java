package com.fernandobarillas.redditservice.models;

import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Thing;
import net.dean.jraw.models.Thumbnails;
import net.dean.jraw.models.VoteDirection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fb on 12/15/15.
 */
public class Link extends PublicContribution {
    private Submission        mSubmission;
    private String            mAuthor;
    private Boolean           mIsNsfw;
    private Boolean           mIsSaved;
    private String            mPermaLink;
    private String            mSubredditName;
    private String            mTitle;
    private String            mThumbnail;
    private Thumbnails        mThumbnails;
    private String            mUrl;
    private VoteDirection     mVoteDirection;
    private ArrayList<String> mAlbumUrls;

    public Link(String url) {
        super(null);
        mUrl = url;
    }

    public Link(Submission submission) {
        super(submission.getDataNode());
        mSubmission = submission;
        mAuthor = submission.getAuthor();
        mIsNsfw = submission.isNsfw();
        mIsSaved = submission.isSaved();
        mPermaLink = submission.getPermalink();
        mSubredditName = submission.getSubredditName();
        mTitle = submission.getTitle();
        mThumbnail = submission.getThumbnail();
        mThumbnails = submission.getThumbnails();
        mUrl = submission.getUrl();
        mVoteDirection = submission.getVote();
        mAlbumUrls = new ArrayList<>();
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
        return this.getId()
                .equals(thing.getId());
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", getId(), getTitle(), getUrl());
    }

    public List<String> getAlbumUrls() {
        return mAlbumUrls;
    }

    public void setAlbumUrls(ArrayList<String> albumUrls) {
        mAlbumUrls = albumUrls;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public int getCommentCount() {
        return mSubmission.getCommentCount();
    }

    public String getDomain() {
        return mSubmission.getDomain();
    }

    public String getPermalink() {
        return mPermaLink;
    }

    public Submission getSubmission() {
        return mSubmission;
    }

    public String getSubredditName() {
        return mSubredditName;
    }

    public String getThumbnail() {
        return mThumbnail;
    }

    public Thumbnails getThumbnails() {
        return mThumbnails;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUrl() {
        return mUrl;
    }

    public VoteDirection getVoteDirection() {
        return mVoteDirection;
    }

    public void setVoteDirection(VoteDirection voteDirection) {
        mVoteDirection = voteDirection;
    }

    public boolean isDownvoted() {
        return getVoteDirection() == VoteDirection.DOWNVOTE;
    }

    public boolean isGilded() {
        return (getTimesGilded() != null && getTimesGilded() > 0);
    }

    public Boolean isNsfw() {
        return mIsNsfw;
    }

    public Boolean isSaved() {
        return mIsSaved;
    }

    public boolean isUpvoted() {
        return getVoteDirection() == VoteDirection.UPVOTE;
    }

    public void setSaved(Boolean isSaved) {
        mIsSaved = isSaved;
    }

    public void setScore(int score) {
    }
}
