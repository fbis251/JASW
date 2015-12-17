package com.fernandobarillas.redditservice.exceptions;

/**
 * Created by fb on 12/15/15.
 */
public class NullRedditClientException extends Exception {
    private final static String DETAIL_MESSAGE = "RedditClient instance is null";

    public NullRedditClientException() {
        super(DETAIL_MESSAGE);
    }
}
