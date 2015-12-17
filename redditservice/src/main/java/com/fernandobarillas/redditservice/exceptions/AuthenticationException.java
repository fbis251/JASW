package com.fernandobarillas.redditservice.exceptions;

/**
 * Created by fb on 12/14/15.
 */
public class AuthenticationException extends Exception {
    private final static String DETAIL_MESSAGE = "Client was unable to authenticate";

    public AuthenticationException() {
        super(DETAIL_MESSAGE);
    }
}
