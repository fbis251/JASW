package com.fernandobarillas.redditservice.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.fernandobarillas.redditservice.exceptions.NullAccountManagerException;
import com.fernandobarillas.redditservice.requests.VoteRequest;

import net.dean.jraw.managers.AccountManager;

/**
 * Created by fb on 12/15/15.
 */
public class VoteTask extends AsyncTask<VoteRequest, Void, Exception> {
    private static final String LOG_TAG = "VoteTask";
    private VoteRequest mVoteRequest;

    @Override
    protected Exception doInBackground(VoteRequest... voteRequests) {
        Log.d(LOG_TAG, "doInBackground()");
        if (voteRequests.length != 1) {
            // TODO: Create custom Exception
            return new Exception("More than 1 vote request passed in");
        }

        mVoteRequest = voteRequests[0];
        AccountManager accountManager = mVoteRequest.getAccountManager();

        if (accountManager == null) {
            return new NullAccountManagerException();
        }

        try {
            accountManager.vote(mVoteRequest.getLink(), mVoteRequest.getVoteDirection());
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "doInBackground: failed", e);
            // Pass back the exception to the caller to have the option of displaying the
            // message in the UI
            return e;
        }
    }

    @Override
    protected void onPostExecute(Exception e) {
        Log.d(LOG_TAG, "onPostExecute() called with: " + "e = [" + e + "]");
        super.onPostExecute(e);

        // Make sure a onComplete has been set
        if (mVoteRequest.getRedditVoteCallback() == null) {
            return;
        }

        // Now that we know the onComplete isn't null, execute it
        mVoteRequest.getRedditVoteCallback().voteCallback(e);
    }
}
