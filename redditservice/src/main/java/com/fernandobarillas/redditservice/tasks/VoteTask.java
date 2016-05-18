package com.fernandobarillas.redditservice.tasks;

import com.fernandobarillas.redditservice.exceptions.NullAccountManagerException;
import com.fernandobarillas.redditservice.requests.VoteRequest;
import com.orhanobut.logger.Logger;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by fb on 12/15/15.
 */
public class VoteTask {
    private AccountManager mAccountManager;
    private VoteRequest    mVoteRequest;

    public VoteTask(AccountManager accountManager) {
        mAccountManager = accountManager;
    }

    public Observable<Boolean> vote(VoteRequest voteRequest) {
        mVoteRequest = voteRequest;
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                if (subscriber.isUnsubscribed()) return;
                try {
                    subscriber.onNext(vote());
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private boolean vote() throws NullAccountManagerException, ApiException {
        Logger.v("doInBackground() called");
        if (mAccountManager == null) {
            throw new NullAccountManagerException();
        }

        mAccountManager.vote(mVoteRequest.getLink(), mVoteRequest.getVoteDirection());
        return true;
    }
}
