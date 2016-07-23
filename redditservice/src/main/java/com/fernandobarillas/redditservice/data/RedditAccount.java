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

    public Observable<Boolean> saveLink(final PublicContribution link, final boolean isSave) {
        Timber.v("saveLink() called with: " + "link = [" + link + "], isSave = [" + isSave + "]");
        SaveRequest saveRequest = new SaveRequest(link, isSave);
        Saving saving = new Saving(mAccountManager);
        return saving.save(saveRequest);
    }

    public Observable<Boolean> voteLink(PublicContribution link, VoteDirection voteDirection) {
        Timber.v("voteLink() called with: "
                + "link = ["
                + link
                + "], voteDirection = ["
                + voteDirection
                + "]");
        VoteRequest voteRequest = new VoteRequest(link, voteDirection);
        Voting voting = new Voting(mAccountManager);
        return voting.vote(voteRequest);
    }
}
