package com.fernandobarillas.redditservice.requests;

import android.support.annotation.IntDef;

import net.dean.jraw.models.PublicContribution;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by fb on 12/15/15.
 */
public class VoteRequest {
    // JRAW Submission VoteDirection as int representations
    public final static int DOWNVOTE = -1;
    public final static int NO_VOTE  = 0;
    public final static int UPVOTE   = 1;

    private PublicContribution mLink;
    private
    @VoteDirection
    int mVoteDirection;

    public VoteRequest(PublicContribution link, @VoteDirection int voteDirection) {
        mLink = link;
        mVoteDirection = voteDirection;
    }

    public VoteRequest(PublicContribution link, net.dean.jraw.models.VoteDirection voteDirection) {
        mLink = link;
        mVoteDirection = NO_VOTE;
        if (voteDirection == net.dean.jraw.models.VoteDirection.UPVOTE) mVoteDirection = UPVOTE;
        if (voteDirection == net.dean.jraw.models.VoteDirection.DOWNVOTE) mVoteDirection = DOWNVOTE;
    }

    public PublicContribution getLink() {
        return mLink;
    }

    public
    @VoteDirection
    int getVoteDirection() {
        return mVoteDirection;
    }

    @IntDef({DOWNVOTE, NO_VOTE, UPVOTE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface VoteDirection {
    }
}
