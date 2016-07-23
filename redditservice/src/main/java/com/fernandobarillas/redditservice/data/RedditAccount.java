package com.fernandobarillas.redditservice.data;

import com.fernandobarillas.redditservice.observables.Saving;
import com.fernandobarillas.redditservice.observables.Voting;
import com.fernandobarillas.redditservice.requests.SaveRequest;
import com.fernandobarillas.redditservice.requests.VoteRequest;

import net.dean.jraw.RedditClient;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.VoteDirection;

import rx.Observable;
import timber.log.Timber;

/**
 * Created by fb on 12/14/15.
 */
public class RedditAccount {
    private RedditClient   mRedditClient;
    private AccountManager mAccountManager;

    public RedditAccount(RedditClient redditClient) {
        Timber.v("RedditAccount() called with: " + "redditClient = [" + redditClient + "]");
        mRedditClient = redditClient;
        mAccountManager = new AccountManager(mRedditClient);
    }

    public Observable<Boolean> saveContribution(final PublicContribution contribution,
            final boolean isSave) {
        Timber.v("saveContribution() called with: "
                + "contribution = ["
                + contribution
                + "], isSave = ["
                + isSave
                + "]");
        SaveRequest saveRequest = new SaveRequest(contribution, isSave);
        Saving saving = new Saving(mAccountManager);
        return saving.save(saveRequest);
    }

    public Observable<Boolean> voteContribution(PublicContribution contribution,
            VoteDirection voteDirection) {
        Timber.v("voteContribution() called with: "
                + "contribution = ["
                + contribution
                + "], voteDirection = ["
                + voteDirection
                + "]");
        VoteRequest voteRequest = new VoteRequest(contribution, voteDirection);
        Voting voting = new Voting(mAccountManager);
        return voting.vote(voteRequest);
    }
}
