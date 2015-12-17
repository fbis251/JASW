package com.fernandobarillas.redditservice.links.validators;

import com.fernandobarillas.redditservice.models.Link;

/**
 * Created by fb on 12/15/15.
 */
public interface LinkValidator {
    boolean isLinkValid(Link link);
}
