package com.fernandobarillas.redditservice.exceptions;

/**
 * Created by fb on 1/25/16.
 */
public class LinksListNullException extends Exception {
    private final static String DETAIL_MESSAGE = "Links List instance was null after API request";

    public LinksListNullException() {
        super(DETAIL_MESSAGE);
    }
}
