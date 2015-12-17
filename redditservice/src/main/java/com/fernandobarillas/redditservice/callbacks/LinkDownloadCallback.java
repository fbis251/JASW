package com.fernandobarillas.redditservice.callbacks;

import com.fernandobarillas.redditservice.models.Link;

import java.util.List;

/**
 * Created by fb on 12/15/15.
 */
public interface LinkDownloadCallback {
    void linkDownloadCallback(List<Link> newLinksList, Exception e);
}
