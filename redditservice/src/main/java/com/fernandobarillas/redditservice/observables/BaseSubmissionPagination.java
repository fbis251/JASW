package com.fernandobarillas.redditservice.observables;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Paginator;

import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by fb on 5/18/16.
 */
abstract class BaseSubmissionPagination {
    static Observable<List<Submission>> getMoreSubmissions(final Paginator<Submission> paginator) {
        if (paginator == null) return null;
        return Observable.create(new Observable.OnSubscribe<List<Submission>>() {
            @Override
            public void call(Subscriber<? super List<Submission>> subscriber) {
                try {
                    if (subscriber.isUnsubscribed()) return;
                    if (paginator.hasNext()) {
                        Listing<Submission> submissionListing = paginator.next(true);
                        if (submissionListing.getChildren() != null) {
                            subscriber.onNext(submissionListing);
                        }
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    if (subscriber.isUnsubscribed()) return;
                    subscriber.onError(e);
                }
            }
        });
    }
}
