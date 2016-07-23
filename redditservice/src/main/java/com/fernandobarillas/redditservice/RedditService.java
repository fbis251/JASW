package com.fernandobarillas.redditservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.fernandobarillas.redditservice.data.RedditData;
import com.fernandobarillas.redditservice.exceptions.ServiceNotReadyException;
import com.fernandobarillas.redditservice.observables.Authentication;
import com.fernandobarillas.redditservice.observables.SubredditPagination;
import com.fernandobarillas.redditservice.preferences.RedditAuthPreferences;
import com.fernandobarillas.redditservice.preferences.ServicePreferences;
import com.fernandobarillas.redditservice.requests.AuthRequest;
import com.fernandobarillas.redditservice.requests.SubredditRequest;
import com.fernandobarillas.redditservice.requests.VoteRequest;
import com.fernandobarillas.redditservice.results.AuthResult;
import com.fernandobarillas.redditservice.results.SaveResult;
import com.fernandobarillas.redditservice.results.VoteResult;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * The main class a client should interact with when making any kind of request to the reddit API.
 * This class can be bound to by several Activities which essentially makes it a Singleton. The
 * advantage of this is that once the Service is initialized it keeps its own client and data
 * instances instantiated which makes each activity that connects only have to deal with the
 * Observables that are returned when making any kind of request. Make sure when binding to this
 * Service to send the correct EXTRAS. You can use the {@link #getRedditServiceIntent(Context,
 * String, String, String, UserAgent) getRedditServiceIntent} method to build a proper Intent
 */
public class RedditService extends Service {
    /** Max links to request from reddit at a time **/
    public static final int    MAX_LINK_LIMIT  = 100;
    /** The base URL to use when building URLs such as links to posts and direct comment links */
    public static final String REDDIT_BASE_URL = "https://reddit.com";

    private static final String USERNAME_KEY            = "username";
    private static final String USER_AGENT_KEY          = "user_agent";
    private static final String REDDIT_CLIENT_ID_KEY    = "reddit_client_id";
    private static final String REDDIT_REDIRECT_URL_KEY = "reddit_redirect_url";

    /**
     * Time to deduct from authentication token expiration time. Gives a good buffer before reddit
     * deauthenticates the client
     */
    private static final int FIVE_MIN_MS = 300000;

    private final IBinder mIBinder = new RedditBinder();

    private RedditAuthPreferences mAuthPreferences;
    private ServicePreferences    mServicePreferences;
    private RedditData            mRedditData;
    private boolean mIsServiceReady = false; // Ready to provide data

    public RedditService() {
        Timber.v("RedditService() called");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.v("onStartCommand() called with: "
                + "intent = ["
                + intent
                + "], flags = ["
                + flags
                + "], startId = ["
                + startId
                + "]");
        initializeService(intent);
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Timber.v("onDestroy() called");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Timber.v("onBind() called with: " + "intent = [" + intent + "]");
        initializeService(intent);

        return mIBinder;
    }

    /**
     * Builds an Intent using the parameters needed when binding to the RedditService
     *
     * @param context      The context to use for building the Intent
     * @param username     The reddit username to use during authentication. Can be null to start a
     *                     user-less session (guest mode)
     * @param clientId     The installed-application client ID
     * @param redirectUrl  The redirect URL for the application
     * @param appUserAgent The user agent to send with all requests made to the reddit API
     * @return An Intent using the parameters needed when binding to the {@link RedditService}
     */
    public static Intent getRedditServiceIntent(Context context, @Nullable String username,
            String clientId, String redirectUrl, UserAgent appUserAgent) {
        Timber.v("getRedditServiceIntent()");

        // Bind the reddit service to this Activity
        Intent redditServiceIntent = new Intent(context, RedditService.class);
        redditServiceIntent.putExtra(RedditService.USERNAME_KEY, username);
        redditServiceIntent.putExtra(RedditService.REDDIT_CLIENT_ID_KEY, clientId);
        redditServiceIntent.putExtra(RedditService.REDDIT_REDIRECT_URL_KEY, redirectUrl);
        redditServiceIntent.putExtra(RedditService.USER_AGENT_KEY, appUserAgent.toString());
        return redditServiceIntent;
    }

    /**
     * Performs an API request to get more submissions from a subreddit
     *
     * @param paginator The paginator to use when making the request
     * @return An Observable that returns the List of Submissions gotten from the reddit API
     * @throws ServiceNotReadyException When the service isn't ready to make requests yet
     */
    public Observable<List<Submission>> getMoreSubmissions(final SubredditPaginator paginator)
            throws ServiceNotReadyException {
        Timber.v("getMoreSubmissions() called with: " + "paginator = [" + paginator + "]");
        validateService();
        return SubredditPagination.getMoreSubmissions(paginator)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Gets the RedditClient instance the library is using to make its requests
     *
     * @return The RedditClient instance the library is using to make its requests
     * @throws ServiceNotReadyException When the service isn't ready to make requests yet
     */
    public RedditClient getRedditClient() throws ServiceNotReadyException {
        Timber.v("getRedditClient() called");
        validateService();
        return mRedditData.mRedditClient;
    }

    /**
     * Gets a new SubredditPaginator instance created using the passed-in SubredditRequest
     *
     * @param subredditRequest The settings to use when instantiating the SubredditPaginator
     * @return A new SubredditPaginator instance created using the passed-in SubredditRequest
     * @throws ServiceNotReadyException When the service isn't ready to make requests yet
     */
    public SubredditPaginator getSubredditPaginator(final SubredditRequest subredditRequest)
            throws ServiceNotReadyException {
        Timber.v("getSubredditPaginator() called with: "
                + "subredditRequest = ["
                + subredditRequest
                + "]");
        validateService();
        return mRedditData.getSubredditPaginator(subredditRequest);
    }

    /**
     * Gets the logged-in user's subscribed subreddits
     *
     * @return An Observable that emits all of the user's subreddit subscriptions
     * @throws ServiceNotReadyException When the service isn't ready to make requests yet
     */
    public Observable<List<Subreddit>> getSubscriptions() throws ServiceNotReadyException {
        Timber.v("getSubscriptions() called");
        validateService();
        return mRedditData.getSubscriptions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Checks whether the service is ready, authenticated and able to start making requests
     *
     * @return True if the service is ready to make requests, false if not ready yet
     */
    public boolean isServiceReady() {
        return mIsServiceReady;
    }

    /**
     * Makes a request to save/unsave the passed-in contribution
     *
     * @param contribution The contribution to save or unsave
     * @param isSave       True to save, false to unsave
     * @return An Observable that emits the result of the save/unsave API request
     * @throws ServiceNotReadyException When the service isn't ready to make requests yet
     */
    public Observable<SaveResult> saveContribution(final PublicContribution contribution,
            final boolean isSave) throws ServiceNotReadyException {
        Timber.v("saveContribution() called with: "
                + "contribution = ["
                + contribution
                + "], isSave = ["
                + isSave
                + "]");
        validateService();
        if (mRedditData == null) throw new ServiceNotReadyException();
        final SaveResult saveResult = new SaveResult(contribution);
        return mRedditData.saveContribution(contribution, isSave)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        Timber.e("saveContribution onSubscribe()");
                        authenticate();
                    }
                })
                .map(new Func1<Boolean, SaveResult>() {
                    @Override
                    public SaveResult call(Boolean aBoolean) {
                        saveResult.setSuccessful(true);
                        return saveResult;
                    }
                })
                .onErrorReturn(new Func1<Throwable, SaveResult>() {
                    @Override
                    public SaveResult call(Throwable throwable) {
                        saveResult.setSuccessful(false);
                        saveResult.setThrowable(throwable);
                        return saveResult;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Makes a vote request on the passed-in contribution
     *
     * @param contribution The contribution to vote on
     * @param direction    The direction to vote, can be {@link VoteRequest#UPVOTE}, {@link
     *                     VoteRequest#DOWNVOTE} or {@link VoteRequest#NO_VOTE}
     * @return An Observable that emits the result of the vote API request
     * @throws ServiceNotReadyException When the service isn't ready to make requests yet
     */
    public Observable<VoteResult> voteContribution(final PublicContribution contribution,
            @VoteRequest.VoteDirection int direction) throws ServiceNotReadyException {
        Timber.v("voteContribution() called with: "
                + "contribution = ["
                + contribution
                + "], direction = ["
                + direction
                + "]");
        validateService();
        final VoteResult voteResult = new VoteResult(contribution);
        VoteDirection voteDirection = VoteDirection.NO_VOTE;
        switch (direction) {
            case VoteRequest.DOWNVOTE:
                voteDirection = VoteDirection.DOWNVOTE;
                break;
            case VoteRequest.UPVOTE:
                voteDirection = VoteDirection.UPVOTE;
                break;
            case VoteRequest.NO_VOTE:
            default:
                break;
        }

        return mRedditData.voteContribution(contribution, voteDirection)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        Timber.e("saveContribution onSubscribe()");
                        authenticate();
                    }
                })
                .map(new Func1<Boolean, VoteResult>() {
                    @Override
                    public VoteResult call(Boolean aBoolean) {
                        voteResult.setSuccessful(true);
                        return voteResult;
                    }
                })
                .onErrorReturn(new Func1<Throwable, VoteResult>() {
                    @Override
                    public VoteResult call(Throwable throwable) {
                        voteResult.setSuccessful(false);
                        voteResult.setThrowable(throwable);
                        return voteResult;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Subscriber<AuthResult> authResultSubscriber() {
        Timber.v("authResultSubscriber() called");
        return new Subscriber<AuthResult>() {
            @Override
            public void onCompleted() {
                Timber.v("authResultSubscriber onCompleted() called");
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "authResultSubscriber onError");
            }

            @Override
            public void onNext(AuthResult authResult) {
                Timber.v("authResultSubscriber onNext() called with: "
                        + "authResult = ["
                        + authResult
                        + "]");
                handleAuthResult(authResult);
            }
        };
    }

    private void authenticate() {
        Timber.v("authenticate() called");
        if (mRedditData.mRedditClient.isAuthenticated() && !Authentication.isExpired(
                mAuthPreferences.getExpirationTime())) {
            return;
        }

        AuthRequest request = getNewAuthRequest();
        Timber.v("authenticate: Auth request " + request);
        Authentication authentication = new Authentication(mRedditData.mRedditClient);
        try {
            handleAuthResult(authentication.authenticate(request));
        } catch (OAuthException e) {
            Timber.e(e, "authenticate() failed");
        }
    }

    private AuthRequest getNewAuthRequest() {
        Timber.v("getNewAuthRequest() called");
        if (mAuthPreferences.getUsername() != null && mAuthPreferences.getRefreshToken() == null) {
            // TODO: Handle non-userless with no refresh token
            Timber.e("getNewAuthRequest: No refresh token found for user [%s]",
                    mAuthPreferences.getUsername());
            return null;
        }
        Timber.v("getNewAuthRequest: Building new auth request for username [%s]",
                mAuthPreferences.getUsername());
        return new AuthRequest(mAuthPreferences.getRefreshToken(),
                mServicePreferences.getRedditClientId(), mServicePreferences.getRedditRedirectUrl(),
                mAuthPreferences.getAuthenticationJson(), mAuthPreferences.getExpirationTime());
    }

    private void handleAuthResult(AuthResult authResult) {
        Timber.v("handleAuthResult() called with: " + "authResult = [" + authResult + "]");
        if (!authResult.isCachedData()) {
            Timber.v("authenticationCallback: Caching new authentication data");
            String authenticationJson = authResult.getAuthenticationJson();
            long currentTime = new Date().getTime();
            long expirationTime = authResult.getExpirationTime() - FIVE_MIN_MS;
            Timber.d("handleAuthResult: Crrnt time: [%s]", new Date(currentTime));
            Timber.d("handleAuthResult: Exprn time: [%s]", new Date(expirationTime));
            Timber.d("handleAuthResult: Auth json: [%s]", authenticationJson);
            if (expirationTime > currentTime && !TextUtils.isEmpty(authenticationJson)) {
                mAuthPreferences.setExpirationTime(expirationTime);
                mAuthPreferences.setAuthenticationJson(authenticationJson);
                mAuthPreferences.commit();
                Timber.i("handleAuthResult: New auth data cached for user [%s]",
                        mAuthPreferences.getUsername());
            } else {
                Timber.i("handleAuthResult: New auth data was NOT cached for user [%s]",
                        mAuthPreferences.getUsername());
            }
        } else {
            Timber.d("handleAuthResult: Auth result already cached. Expiration: [%s]",
                    new Date(mAuthPreferences.getExpirationTime()));
        }
        mIsServiceReady = true;
    }

    private void initializeService(Intent intent) {
        Timber.v("initializeService() called with: " + "intent = [" + intent + "]");

        Context serviceContext = this;

        String username = intent.getExtras().getString(USERNAME_KEY, null);
        String userAgentString =
                intent.getExtras().getString(USER_AGENT_KEY, Constants.DEFAULT_USER_AGENT);
        String redditClientId = intent.getExtras().getString(REDDIT_CLIENT_ID_KEY, null);
        String redditRedirectUrl = intent.getExtras().getString(REDDIT_REDIRECT_URL_KEY, null);

        if (redditClientId == null || redditRedirectUrl == null) {
            // TODO: Handle null extras
            return;
        }

        mAuthPreferences = new RedditAuthPreferences(serviceContext, username);
        Timber.i("initializeService: Loaded preferences for user [%s]",
                mAuthPreferences.getUsername());
        mServicePreferences = new ServicePreferences(serviceContext);

        mServicePreferences.setUserAgentString(userAgentString);
        mServicePreferences.setRedditClientId(redditClientId);
        mServicePreferences.setRedditRedirectUrl(redditRedirectUrl);

        mRedditData = new RedditData(UserAgent.of(mServicePreferences.getUserAgentString()));

        if (!mRedditData.mRedditClient.isAuthenticated()) {
            Authentication authentication = new Authentication(mRedditData.mRedditClient);
            AuthRequest request = getNewAuthRequest();
            Timber.d("initializeService: Auth request " + request);
            authentication.asyncAuthenticate(request)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(authResultSubscriber());
        }
    }

    private void validateService() throws ServiceNotReadyException {
        if (mRedditData == null || mRedditData.mRedditClient == null) {
            throw new ServiceNotReadyException();
        }
    }

    public class RedditBinder extends Binder {
        public RedditService getService() {
            Timber.v("getService() called");
            return RedditService.this;
        }
    }
}

