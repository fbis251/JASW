package com.fernandobarillas.redditservice.observables;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.DomainPaginator;

import java.util.List;

import rx.Observable;

/**
 * Created by fb on 5/18/16.
 */
public class DomainPagination extends BaseSubmissionPagination {
    public static Observable<List<Submission>> getMoreSubmissions(final DomainPaginator paginator) {
        return BaseSubmissionPagination.getMoreSubmissions(paginator);
    }
}
