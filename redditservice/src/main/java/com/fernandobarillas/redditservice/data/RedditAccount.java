package com.fernandobarillas.redditservice.data;

import com.fernandobarillas.redditservice.models.Link;
import com.fernandobarillas.redditservice.observables.Voting;
import com.fernandobarillas.redditservice.requests.SaveRequest;
import com.fernandobarillas.redditservice.requests.VoteRequest;
import com.fernandobarillas.redditservice.tasks.SaveTask;
import com.orhanobut.logger.Logger;

import net.dean.jraw.RedditClient;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.VoteDirection;

import rx.Observable;

/**
 * Created by fb on 12/14/15.
 */
public class RedditAccount {
    private RedditClient   mRedditClient;
    private AccountManager mAccountManager;

    public RedditAccount(RedditClient redditClient) {
        Logger.v("RedditAccount() called with: " + "redditClient = [" + redditClient + "]");
        mRedditClient = redditClient;
        mAccountManager = new AccountManager(mRedditClient);
    }

    public Observable<Boolean> saveLink(final Link link, final boolean isSave) {
        Logger.v("saveLink() called with: " + "link = [" + link + "], isSave = [" + isSave + "]");
        SaveRequest saveRequest = new SaveRequest(link, isSave);
        SaveTask    saveTask    = new SaveTask(mAccountManager);
        return saveTask.save(saveRequest);
    }

    public Observable<Boolean> voteLink(Link link, VoteDirection voteDirection) {
        Logger.v("voteLink() called with: " + "link = [" + link + "], voteDirection = [" + voteDirection + "]");
        VoteRequest voteRequest = new VoteRequest(link, voteDirection);
        Voting      voting      = new Voting(mAccountManager);
        return voting.vote(voteRequest);
    }
}
