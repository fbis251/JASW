package com.fernandobarillas.redditservice.requests;

import com.fernandobarillas.redditservice.callbacks.RedditVoteCallback;
import com.fernandobarillas.redditservice.models.Link;

import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.VoteDirection;

/**
 * Created by fb on 12/15/15.
 */
public class VoteRequest {
    private Link mLink;
    private VoteDirection mVoteDirection;
    private AccountManager mAccountManager;
    private RedditVoteCallback mRedditVoteCallback;

    public VoteRequest(Link link, VoteDirection voteDirection, AccountManager accountManager,
                       RedditVoteCallback redditVoteCallback) {
        mLink = link;
        mVoteDirection = voteDirection;
        mAccountManager = accountManager;
        mRedditVoteCallback = redditVoteCallback;
    }

    public AccountManager getAccountManager() {
        return mAccountManager;
    }

    public Link getLink() {
        return mLink;
    }

    public RedditVoteCallback getRedditVoteCallback() {
        return mRedditVoteCallback;
    }

    public VoteDirection getVoteDirection() {
        return mVoteDirection;
    }
}
