package com.fernandobarillas.redditservice.requests;

import com.fernandobarillas.redditservice.models.Link;

/**
 * Created by fb on 12/15/15.
 */
public class SaveRequest {
    public static final boolean SAVE   = true;
    public static final boolean UNSAVE = false;
    private Link    mLink;
    private boolean mSave; // True to save, false to unsave

    public SaveRequest(Link link, boolean save) {
        mLink = link;
        mSave = save;
    }

    public Link getLink() {
        return mLink;
    }

    public boolean isSave() {
        return mSave;
    }
}
