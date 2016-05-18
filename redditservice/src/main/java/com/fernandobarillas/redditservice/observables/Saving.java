package com.fernandobarillas.redditservice.observables;

import com.fernandobarillas.redditservice.exceptions.NullAccountManagerException;
import com.fernandobarillas.redditservice.exceptions.SameSaveStateException;
import com.fernandobarillas.redditservice.requests.SaveRequest;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by fb on 12/15/15.
 */
public class Saving {
    private SaveRequest    mSaveRequest;
    private AccountManager mAccountManager;

    public Saving(AccountManager accountManager) {
        mAccountManager = accountManager;
    }

    public Observable<Boolean> save(SaveRequest saveRequest) {
        mSaveRequest = saveRequest;
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    boolean result = save();
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

    private boolean save() throws NullAccountManagerException, ApiException, SameSaveStateException {
        if (mAccountManager == null) {
            throw new NullAccountManagerException();
        }

        boolean isSaveRequest = mSaveRequest.isSave();
        boolean currentSaveState = mSaveRequest.getLink()
                .isSaved();
        if (isSaveRequest == currentSaveState) {
            throw new SameSaveStateException();
        }

        if (isSaveRequest) {
            mAccountManager.save(mSaveRequest.getLink());
        } else {
            mAccountManager.unsave(mSaveRequest.getLink());
        }

        return true;
    }
}
