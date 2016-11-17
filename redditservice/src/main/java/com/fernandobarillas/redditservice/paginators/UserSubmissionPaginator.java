package com.fernandobarillas.redditservice.paginators;

import net.dean.jraw.EndpointImplementation;
import net.dean.jraw.Endpoints;
import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.GenericPaginator;

/**
 * This class is used to paginate through user posts or comments via {@code
 * /user/{username}/{where}.json}
 */
public class UserSubmissionPaginator extends GenericPaginator<Submission> {
    private String username;

    /**
     * Instantiates a new UserPaginatorSubmission
     *
     * @param creator  The RedditClient that will be used to send HTTP requests
     * @param username The user to view
     */
    public UserSubmissionPaginator(RedditClient creator, String username) {
        super(creator, Submission.class, "submitted");
        this.username = username;
    }

    @Override
    public String getUriPrefix() {
        return "/user/" + username;
    }

    @Override
    public String[] getWhereValues() {
        return new String[]{
                "overview",
                "gilded",
                "submitted",
                "liked",
                "disliked",
                "hidden",
                "saved",
                "comments"
        };
    }

    @Override
    @EndpointImplementation({
            Endpoints.USER_USERNAME_SUBMITTED
    })
    public Listing<Submission> next(boolean forceNetwork) {
        // Just call super so that we can add the @EndpointImplementation annotation
        return super.next(forceNetwork);
    }

    /**
     * Gets the name whose submitted links you are iterating over
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }
}
