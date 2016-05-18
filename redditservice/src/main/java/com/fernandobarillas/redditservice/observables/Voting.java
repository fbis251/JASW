package com.fernandobarillas.redditservice.observables;

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
public class Voting {
    private AccountManager mAccountManager;
    private VoteRequest    mVoteRequest;

    public Voting(AccountManager accountManager) {
        mAccountManager = accountManager;
    }

    public Observable<Boolean> vote(VoteRequest voteRequest) {
        mVoteRequest = voteRequest;
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    boolean result = vote();
                    if (subscriber.isUnsubscribed()) return;
                    subscriber.onNext(result);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    if (subscriber.isUnsubscribed()) return;
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
