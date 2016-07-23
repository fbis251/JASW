package com.fernandobarillas.redditservice.results;

import net.dean.jraw.models.PublicContribution;

/**
 * Created by fb on 5/1/16.
 */
public class SaveResult extends BaseAccountResult {
    public SaveResult(PublicContribution link) {
        super(link);
    }
}
