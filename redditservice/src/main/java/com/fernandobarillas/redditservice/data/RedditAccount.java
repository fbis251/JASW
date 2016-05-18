package com.fernandobarillas.redditservice.data;

import android.util.Log;

import com.fernandobarillas.redditservice.callbacks.RedditSaveCallback;
import com.fernandobarillas.redditservice.models.Link;
import com.fernandobarillas.redditservice.observables.Voting;
import com.fernandobarillas.redditservice.requests.SaveRequest;
import com.fernandobarillas.redditservice.requests.VoteRequest;
import com.fernandobarillas.redditservice.tasks.SaveTask;

import net.dean.jraw.RedditClient;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.VoteDirection;

import rx.Observable;

/**
 * Created by fb on 12/14/15.
 */
public class RedditAccount {
    private static final String LOG_TAG = "RedditAccount";
    private RedditClient   mRedditClient;
    private AccountManager mAccountManager;

    public RedditAccount(RedditClient redditClient) {
        mRedditClient = redditClient;
        mAccountManager = new AccountManager(mRedditClient);
    }

    public void saveLink(final Link link, final RedditSaveCallback saveCallback) {
        Log.d(LOG_TAG,
                "saveLink() called with: " + "link = [" + link + "], saveCallback = [" + saveCallback + "]");
        saveLink(link, SaveRequest.SAVE, saveCallback);
    }

    private void saveLink(final Link link, boolean doSave, final RedditSaveCallback saveCallback) {
        // TODO: Use a FIFO queue for any saving?
        SaveRequest saveRequest = new SaveRequest(link, doSave, mAccountManager, saveCallback);
        SaveTask saveTask = new SaveTask();
        saveTask.execute(saveRequest);
    }

    public void unsaveLink(final Link link, final RedditSaveCallback saveCallback) {
        Log.d(LOG_TAG,
                "unsaveLink() called with: " + "link = [" + link + "], saveCallback = [" + saveCallback + "]");
        saveLink(link, SaveRequest.UNSAVE, saveCallback);
    }

    public Observable<Boolean> voteLink(Link link, VoteDirection voteDirection) {
        VoteRequest voteRequest = new VoteRequest(link, voteDirection);
        Voting      voting      = new Voting(mAccountManager);
        return voting.vote(voteRequest);
    }
}
