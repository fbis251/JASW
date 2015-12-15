package com.fernandobarillas.redditservice.exceptions;

/**
 * Created by fb on 12/15/15.
 */
public class NullAccountManagerException extends Exception {
    private final static String detailMessage = "AccountManager instance is null";

    public NullAccountManagerException() {
        super(detailMessage);
    }
}
