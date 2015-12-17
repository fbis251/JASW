package com.fernandobarillas.redditservice.links.filters;

import com.fernandobarillas.redditservice.models.Link;

import java.util.ArrayList;

/**
 * Created by fb on 12/15/15.
 */
public interface LinkFilter {
    ArrayList<Link> filterLinks(ArrayList<Link> unfilteredLinksList);
}
