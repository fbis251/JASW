package com.fernandobarillas.redditservice.observables;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Paginator;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.List;

import rx.Observable;

/**
 * Created by fb on 5/18/16.
 */
public class SubredditPagination extends BaseSubmissionPagination {
    public static Observable<List<Submission>> getMoreSubmissions(
            final Paginator<Submission> paginator) {
        return BaseSubmissionPagination.getMoreSubmissions(paginator);
    }
}
