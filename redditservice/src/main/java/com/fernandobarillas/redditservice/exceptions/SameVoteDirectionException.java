package com.fernandobarillas.redditservice.exceptions;

/**
 * Created by fb on 12/15/15.
 */
public class SameVoteDirectionException extends Exception {
    private final static String DETAIL_MESSAGE =
            "Current Submission VoteDirection is the same as VoteDirection request";

    public SameVoteDirectionException() {
        super(DETAIL_MESSAGE);
    }
}
