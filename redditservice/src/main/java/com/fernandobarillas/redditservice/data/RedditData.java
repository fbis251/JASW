package com.fernandobarillas.redditservice.data;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.fernandobarillas.redditservice.observables.UserSubscriptions;
import com.fernandobarillas.redditservice.paginators.UserSubmissionPaginator;
import com.fernandobarillas.redditservice.requests.SubredditRequest;
import com.fernandobarillas.redditservice.requests.UserSubmissionsRequest;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.LoggingMode;
import net.dean.jraw.http.OkHttpAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import rx.Observable;
import timber.log.Timber;

/**
 * Class that handles providing data for and instantiating reddit {@link
 * net.dean.jraw.paginators.Paginator}s
 */
public class RedditData {
    /** Download attempts before giving up */
    private static final int DOWNLOAD_RETRIES = 2;

    public RedditClient mRedditClient;

    private RedditAccount mRedditAccount;

    public RedditData(UserAgent userAgent, @Nullable final OkHttpClient okHttpClient) {
        Timber.v("RedditData() called with: " + "userAgent = [" + userAgent + "]");

        if (okHttpClient != null) {
            OkHttpAdapter adapter = new OkHttpAdapter(okHttpClient, Protocol.HTTP_2);
            adapter.setRawJson(true);
            mRedditClient = new RedditClient(userAgent, adapter);
        } else {
            mRedditClient = new RedditClient(userAgent);
        }

        mRedditClient.setLoggingMode(LoggingMode.ON_FAIL);
        mRedditClient.setRetryLimit(DOWNLOAD_RETRIES);

        mRedditAccount = new RedditAccount(mRedditClient);
    }

    /**
     * Instantiates a new SubredditPaginator using the current RedditClient instance
     *
     * @param subredditRequest The request data to use when instantiating a new paginator
     * @return A SubredditPaginator instance using the parameters passed-in via the SubredditRequest
     */
    public final SubredditPaginator getSubredditPaginator(final SubredditRequest subredditRequest) {
        SubredditPaginator paginator;
        String subreddit = subredditRequest.getSubreddit();
        if (TextUtils.isEmpty(subreddit)) {
            // Load the frontpage
            Timber.d("getSubredditPaginator:  New Frontpage Paginator");
            paginator = new SubredditPaginator(mRedditClient);
        } else {
            Timber.d("getSubredditPaginator:  New /r/%s Paginator", subreddit);
            paginator = new SubredditPaginator(mRedditClient, subreddit);
        }

        paginator.setLimit(subredditRequest.getLinkLimit());
        paginator.setSorting(subredditRequest.getSorting());
        paginator.setAfter(subredditRequest.getAfter());
        paginator.setTimePeriod(subredditRequest.getTimePeriod());

        return paginator;
    }

    /**
     * @return An Observable for a user's subscribed subreddits
     */
    public Observable<List<Subreddit>> getSubscriptions() {
        UserSubscriptions subredditsObservable = new UserSubscriptions(mRedditClient);
        return subredditsObservable.getSubscriptions();
    }

    /**
     * Instantiates a new UserSubmissionPaginator using the current RedditClient instance
     *
     * @param userRequest The request data to use when instantiating a new paginator
     * @return A UserSubmissionPaginator instance using the parameters passed-in via the
     * SubredditRequest
     */
    public final UserSubmissionPaginator getUserSubmissionsPaginator(final UserSubmissionsRequest userRequest) {
        String username = userRequest.getUsername();
        Timber.d("getSubredditPaginator:  New /u/%s Paginator", username);

        UserSubmissionPaginator paginator = new UserSubmissionPaginator(mRedditClient, username);
        paginator.setLimit(userRequest.getLinkLimit());
        paginator.setSorting(userRequest.getSorting());
        paginator.setAfter(userRequest.getAfter());
        paginator.setTimePeriod(userRequest.getTimePeriod());

        return paginator;
    }

    /**
     * @param contribution The contribution to save/unsave
     * @param isSave       True if you want to save the contribution, false if you want to unsave
     *                     (remove it from your saved list)
     * @return An Observable for the result of the save request, true if successful, false otherwise
     */
    public Observable<Boolean> saveContribution(final PublicContribution contribution,
            final boolean isSave) {
        return mRedditAccount.saveContribution(contribution, isSave);
    }

    /**
     * @param contribution  The contribution to vote on
     * @param voteDirection The vote type to cast on the contribution. Can be upvote, downvote or no
     *                      vote
     * @return An Observable for the result of the vote request, true if successful, false otherwise
     */
    public Observable<Boolean> voteContribution(final PublicContribution contribution,
            final VoteDirection voteDirection) {
        return mRedditAccount.voteContribution(contribution, voteDirection);
    }
}
