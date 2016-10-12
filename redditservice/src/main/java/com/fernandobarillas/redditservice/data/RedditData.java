package com.fernandobarillas.redditservice.data;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.fernandobarillas.redditservice.observables.UserSubscriptions;
import com.fernandobarillas.redditservice.requests.SubredditRequest;

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
 * Created by fb on 12/14/15.
 */
public class RedditData {
    /** Download attempts before giving up */
    public static final int DOWNLOAD_RETRIES = 1;

    public RedditClient mRedditClient;

    private RedditAccount mRedditAccount;

    public RedditData(UserAgent userAgent, @Nullable final OkHttpClient okHttpClient) {
        Timber.v("RedditData() called with: " + "userAgent = [" + userAgent + "]");

        if (okHttpClient != null) {
            OkHttpAdapter adapter = new OkHttpAdapter(okHttpClient, Protocol.HTTP_2);
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
            Timber.i("getSubredditPaginator:  New Frontpage Paginator");
            paginator = new SubredditPaginator(mRedditClient);
        } else {
            Timber.i("getSubredditPaginator:  New /r/%s Paginator", subreddit);
            paginator = new SubredditPaginator(mRedditClient, subreddit);
        }

        paginator.setLimit(subredditRequest.getLinkLimit());
        paginator.setSorting(subredditRequest.getSorting());
        if (subredditRequest.getTimePeriod() != null) {
            paginator.setTimePeriod(subredditRequest.getTimePeriod());
        }

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
