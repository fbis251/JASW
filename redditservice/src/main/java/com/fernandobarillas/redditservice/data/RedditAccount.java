package com.fernandobarillas.redditservice.data;

import android.util.Log;

import com.fernandobarillas.redditservice.callbacks.RedditSaveCallback;
import com.fernandobarillas.redditservice.callbacks.RedditVoteCallback;
import com.fernandobarillas.redditservice.exceptions.SameVoteDirectionException;
import com.fernandobarillas.redditservice.models.Link;
import com.fernandobarillas.redditservice.requests.SaveRequest;
import com.fernandobarillas.redditservice.requests.VoteRequest;
import com.fernandobarillas.redditservice.tasks.SaveTask;
import com.fernandobarillas.redditservice.tasks.VoteTask;

import net.dean.jraw.RedditClient;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.VoteDirection;

/**
 * Created by fb on 12/14/15.
 */
public class RedditAccount {
    private static final String LOG_TAG = "RedditAccount";
    private RedditClient mRedditClient;
    private AccountManager mAccountManager;

    public RedditAccount(RedditClient redditClient) {
        mRedditClient = redditClient;
        mAccountManager = new AccountManager(mRedditClient);
    }

    public void upvoteLink(Link link, RedditVoteCallback voteCallback) {
        Log.d(LOG_TAG, "upvoteLink() called with: " + "link = [" + link + "], voteCallback = [" + voteCallback + "]");
        voteLink(link, VoteDirection.UPVOTE, voteCallback);
    }

    public void downvoteLink(Link link, RedditVoteCallback voteCallback) {
        Log.d(LOG_TAG, "downvoteLink() called with: " + "link = [" + link + "], voteCallback = [" + voteCallback + "]");
        voteLink(link, VoteDirection.DOWNVOTE, voteCallback);
    }

    public void removeVote(Link link, RedditVoteCallback voteCallback) {
        Log.d(LOG_TAG, "removeVote() called with: " + "link = [" + link + "], voteCallback = [" + voteCallback + "]");
        voteLink(link, VoteDirection.NO_VOTE, voteCallback);
    }

    public void saveLink(Link link, RedditSaveCallback saveCallback) {
        Log.d(LOG_TAG, "saveLink() called with: " + "link = [" + link + "], saveCallback = [" + saveCallback + "]");
        saveLink(link, SaveRequest.SAVE, saveCallback);
    }

    public void unsaveLink(Link link, RedditSaveCallback saveCallback) {
        Log.d(LOG_TAG, "unsaveLink() called with: " + "link = [" + link + "], saveCallback = [" + saveCallback + "]");
        saveLink(link, SaveRequest.UNSAVE, saveCallback);
    }

    private void saveLink(Link link, boolean doSave, RedditSaveCallback saveCallback) {
        // TODO: Use a FIFO queue for any saving
        if (link.isSaved() == doSave) {
            saveCallback.saveCallback(new SameVoteDirectionException());
        }
        SaveRequest saveRequest = new SaveRequest(link, doSave, mAccountManager, saveCallback);
        SaveTask saveTask = new SaveTask();
        saveTask.execute(saveRequest);
    }

    private void voteLink(Link link, VoteDirection voteDirection, RedditVoteCallback voteCallback) {
        // TODO: Use a FIFO queue for any voting
        if (link.getVote() == voteDirection && voteCallback != null) {
            voteCallback.voteCallback(new SameVoteDirectionException());
            return;
        }
        VoteRequest voteRequest = new VoteRequest(link, voteDirection, mAccountManager, voteCallback);
        VoteTask voteTask = new VoteTask();
        voteTask.execute(voteRequest);
    }
}
