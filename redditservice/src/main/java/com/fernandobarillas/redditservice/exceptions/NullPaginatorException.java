package com.fernandobarillas.redditservice.exceptions;

/**
 * Created by fb on 12/15/15.
 */
public class NullPaginatorException extends Exception {
    private final static String DETAIL_MESSAGE = "Paginator instance is null";

    public NullPaginatorException() {
        super(DETAIL_MESSAGE);
    }
}
