package com.fernandobarillas.redditservice.exceptions;

/**
 * Created by fb on 12/15/15.
 */
public class SameVoteDirectionException extends Exception {
    private final static String detailMessage = "Current Submission VoteDirection is the same as VoteDirection request";

    public SameVoteDirectionException() {
        super(detailMessage);
    }
}
