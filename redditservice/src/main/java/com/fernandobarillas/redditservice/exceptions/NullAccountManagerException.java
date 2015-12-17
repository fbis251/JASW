package com.fernandobarillas.redditservice.exceptions;

/**
 * Created by fb on 12/15/15.
 */
public class NullAccountManagerException extends Exception {
    private final static String DETAIL_MESSAGE = "AccountManager instance is null";

    public NullAccountManagerException() {
        super(DETAIL_MESSAGE);
    }
}
