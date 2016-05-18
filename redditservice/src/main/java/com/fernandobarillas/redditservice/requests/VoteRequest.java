package com.fernandobarillas.redditservice.requests;

import com.fernandobarillas.redditservice.models.Link;

import net.dean.jraw.models.VoteDirection;

/**
 * Created by fb on 12/15/15.
 */
public class VoteRequest {
    private Link          mLink;
    private VoteDirection mVoteDirection;

    public VoteRequest(Link link, VoteDirection voteDirection) {
        mLink = link;
        mVoteDirection = voteDirection;
    }

    public Link getLink() {
        return mLink;
    }

    public VoteDirection getVoteDirection() {
        return mVoteDirection;
    }
}
