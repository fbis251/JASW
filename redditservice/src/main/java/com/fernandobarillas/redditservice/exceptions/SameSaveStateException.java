package com.fernandobarillas.redditservice.exceptions;

/**
 * Created by fb on 12/15/15.
 */
public class SameSaveStateException extends Exception {
    private final static String DETAIL_MESSAGE = "Submission is save/unsave state matches requested save state";

    public SameSaveStateException() {
        super(DETAIL_MESSAGE);
    }
}
