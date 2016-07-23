package com.fernandobarillas.redditservice.requests;

import net.dean.jraw.models.PublicContribution;

/**
 * Created by fb on 12/15/15.
 */
public class SaveRequest {
    public static final boolean SAVE   = true;
    public static final boolean UNSAVE = false;
    private PublicContribution mLink;
    private boolean            mSave; // True to save, false to unsave

    public SaveRequest(PublicContribution link, boolean save) {
        mLink = link;
        mSave = save;
    }

    public PublicContribution getLink() {
        return mLink;
    }

    public boolean isSave() {
        return mSave;
    }
}
