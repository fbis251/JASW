package com.fernandobarillas.redditservice.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.fernandobarillas.redditservice.exceptions.NullAccountManagerException;
import com.fernandobarillas.redditservice.requests.SaveRequest;

import net.dean.jraw.managers.AccountManager;

/**
 * Created by fb on 12/15/15.
 */
public class SaveTask extends AsyncTask<SaveRequest, Void, Exception> {
    private static final String LOG_TAG = "VoteTask";
    private SaveRequest mSaveRequest;

    @Override
    protected Exception doInBackground(SaveRequest... saveRequests) {
        Log.d(LOG_TAG, "doInBackground()");
        if (saveRequests.length != 1) {
            // TODO: Create custom Exception
            return new Exception("More than 1 vote request passed in");
        }

        mSaveRequest = saveRequests[0];
        AccountManager accountManager = mSaveRequest.getAccountManager();

        if (accountManager == null) {
            return new NullAccountManagerException();
        }

        try {
            if (mSaveRequest.doSave()) {
                mSaveRequest.getAccountManager().save(mSaveRequest.getLink());
            } else {
                mSaveRequest.getAccountManager().unsave(mSaveRequest.getLink());
            }

            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "doInBackground: failed", e);
            return e;
        }
    }

    @Override
    protected void onPostExecute(Exception e) {
        Log.d(LOG_TAG, "onPostExecute() called with: " + "e = [" + e + "]");
        super.onPostExecute(e);

        // Make sure a callback has been set
        if (mSaveRequest.getRedditSaveCallback() == null) {
            return;
        }

        // Now that we know the callback isn't null, execute it
        mSaveRequest.getRedditSaveCallback().saveCallback(e);
    }
}
