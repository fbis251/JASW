package com.fernandobarillas.redditservice.observables;

import com.fernandobarillas.redditservice.exceptions.NullAccountManagerException;
import com.fernandobarillas.redditservice.requests.VoteRequest;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.VoteDirection;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

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
        Timber.v("doInBackground() called");
        if (mAccountManager == null) {
            throw new NullAccountManagerException();
        }

        VoteDirection voteDirection = VoteDirection.NO_VOTE;
        switch (mVoteRequest.getVoteDirection()) {
            case VoteRequest.DOWNVOTE:
                voteDirection = VoteDirection.DOWNVOTE;
                break;
            case VoteRequest.UPVOTE:
                voteDirection = VoteDirection.UPVOTE;
                break;
            case VoteRequest.NO_VOTE:
            default:
                break;
        }

        mAccountManager.vote(mVoteRequest.getLink(), voteDirection);
        return true;
    }
}
