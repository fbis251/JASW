package com.fernandobarillas.redditservice.results;

import net.dean.jraw.models.PublicContribution;

/**
 * Created by fb on 5/1/16.
 */
public class VoteResult extends BaseAccountResult {
    public VoteResult(PublicContribution link) {
        super(link);
    }
}
