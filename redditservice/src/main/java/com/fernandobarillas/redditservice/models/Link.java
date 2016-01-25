package com.fernandobarillas.redditservice.models;

import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Thing;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fb on 12/15/15.
 */
public class Link extends PublicContribution {
    private String mAuthor;
    private Boolean mIsNsfw;
    private Boolean mIsSaved;
    private String mPermaLink;
    private String mSubredditName;
    private String mTitle;
    private String mUrl;
    private ArrayList<String> mAlbumUrls;

    public Link(Submission submission) {
        super(submission.getDataNode());
        mAuthor = submission.getAuthor();
        mIsNsfw = submission.isNsfw();
        mIsSaved = submission.isSaved();
        mPermaLink = submission.getPermalink();
        mSubredditName = submission.getSubredditName();
        mTitle = submission.getTitle();
        mUrl = submission.getUrl();
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
        return this.getId().equals(thing.getId());
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

    public String getPermalink() {
        return mPermaLink;
    }

    public String getSubredditName() {
        return mSubredditName;
    }

    public String getTitle() {
        return mTitle;
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
