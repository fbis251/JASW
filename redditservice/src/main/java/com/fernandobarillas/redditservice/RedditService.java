package com.fernandobarillas.redditservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.fernandobarillas.redditservice.data.RedditData;
import com.fernandobarillas.redditservice.exceptions.ServiceNotReadyException;
import com.fernandobarillas.redditservice.observables.Authentication;
import com.fernandobarillas.redditservice.observables.DomainPagination;
import com.fernandobarillas.redditservice.observables.OauthLogin;
import com.fernandobarillas.redditservice.observables.SubredditPagination;
import com.fernandobarillas.redditservice.paginators.UserSubmissionPaginator;
import com.fernandobarillas.redditservice.preferences.RedditAuthPreferences;
import com.fernandobarillas.redditservice.preferences.ServicePreferences;
import com.fernandobarillas.redditservice.requests.AuthRequest;
import com.fernandobarillas.redditservice.requests.OauthLoginRequest;
import com.fernandobarillas.redditservice.requests.StartServiceRequest;
import com.fernandobarillas.redditservice.requests.SubredditRequest;
import com.fernandobarillas.redditservice.requests.UserSubmissionsRequest;
import com.fernandobarillas.redditservice.requests.VoteRequest;
import com.fernandobarillas.redditservice.results.AuthResult;
import com.fernandobarillas.redditservice.results.OauthLoginResult;
import com.fernandobarillas.redditservice.results.SaveResult;
import com.fernandobarillas.redditservice.results.VoteResult;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.paginators.DomainPaginator;
import net.dean.jraw.paginators.Paginator;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
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
 * instances instantiated which makes each Activity that binds only have to deal with the
 * Observables that are returned when making any kind of request. Make sure that after binding to
 * this class, you call {@link #startService(StartServiceRequest)} before making any requests so
 * that the Service can be prepared to make requests. You can then call {@link #isReadyCheck()} to
 * wait for the Service to authenticate and be notified when it is ready to start taking requests.
 * If you would like to use a custom OkHttpClient instance for all reddit API requests, you can set
 * one using {@link #setOkHttpClient(OkHttpClient)} BEFORE your call to {@link
 * #startService(StartServiceRequest)}.
 */
public class RedditService extends Service {
    /**
     * Max links to request from reddit at a time
     **/
    public static final int    MAX_LINK_LIMIT  = 100;
    /**
     * The base URL to use when building URLs such as links to posts and direct comment links
     */
    public static final String REDDIT_BASE_URL = "https://reddit.com";

    public static final String REDDIT_WWW_URL = "https://www.reddit.com";

    /**
     * Time to deduct from authentication token expiration time. Gives a good buffer before reddit
     * deauthenticates the client
     */
    private static final long FIVE_MINUTES_IN_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private final IBinder mIBinder = new RedditBinder();

    private OkHttpClient          mOkHttpClient;
    private RedditAuthPreferences mAuthPreferences;
    private ServicePreferences    mServicePreferences;
    private RedditData            mRedditData;

    // Service lifecycle
    private boolean mIsServiceInitialized = false; // Service provided with reddit client data
    private boolean mIsServiceReady       = false; // Ready to provide data

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
        return mIBinder;
    }

    /**
     * Gets an Intent to bind to the service using {@link #bindService(Intent, ServiceConnection,
     * int)}
     *
     * @param context The context to use, normally you want this to be either your Application or
     *                your Activity context depending on how you handle your lifecycle
     * @return An Intent used to bind to the service
     */
    public static Intent getRedditServiceIntent(Context context) {
        Timber.v("getRedditServiceIntent()");
        return new Intent(context, RedditService.class);
    }

    /**
     * Performs an API request to get more submissions from a subreddit
     *
     * @param paginator The paginator to use when making the request
     * @return An Observable that returns the List of Submissions gotten from the reddit API
     * @throws ServiceNotReadyException When the service isn't ready to make requests yet
     */
    public Observable<Submission> getMoreSubmissions(final Paginator<Submission> paginator)
            throws ServiceNotReadyException {
        Timber.v("getMoreSubmissions() called with: " + "paginator = [" + paginator + "]");
        validateService();
        return SubredditPagination.getMoreSubmissions(paginator).doOnSubscribe(new Action0() {
            @Override
            public void call() {
                Timber.v("getMoreSubmissions onSubscribe()");
                authenticate();
            }
        }).concatMap(new Func1<List<Submission>, Observable<? extends Submission>>() {
            @Override
            public Observable<? extends Submission> call(List<Submission> submissions) {
                // Map the List of Submissions instead a stream of Submission Objects
                return Observable.from(submissions);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Performs an API request to get more submissions from a paginator
     *
     * @param paginator The paginator to use when making the request
     * @return An Observable that returns the List of Submissions gotten from the reddit API
     * @throws ServiceNotReadyException When the service isn't ready to make requests yet
     */
    public Observable<Submission> getMoreSubmissions(final DomainPaginator paginator)
            throws ServiceNotReadyException {
        Timber.v("getMoreSubmissions() called with: " + "paginator = [" + paginator + "]");
        validateService();
        return DomainPagination.getMoreSubmissions(paginator).doOnSubscribe(new Action0() {
            @Override
            public void call() {
                Timber.v("getMoreSubmissions onSubscribe()");
                authenticate();
            }
        }).concatMap(new Func1<List<Submission>, Observable<? extends Submission>>() {
            @Override
            public Observable<? extends Submission> call(List<Submission> submissions) {
                // Map the List of Submissions instead a stream of Submission Objects
                return Observable.from(submissions);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
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
     * Gets a new UserSubmissionPaginator instance created using the passed-in SubredditRequest
     *
     * @param userRequest The request data to use when instantiating a new paginator
     * @return A UserSubmissionPaginator instance using the parameters passed-in via the
     * SubredditRequest
     * @throws ServiceNotReadyException When the service isn't ready to make requests yet
     */
    public UserSubmissionPaginator getUserSubmissionsPaginator(final UserSubmissionsRequest userRequest)
            throws ServiceNotReadyException {
        Timber.v("getUserSubmissionsPaginator() called with: "
                + "userRequest = ["
                + userRequest
                + "]");
        validateService();
        return mRedditData.getUserSubmissionsPaginator(userRequest);
    }

    /**
     * Checks once per second whether the service is ready, authenticated and able to start making
     * requests
     *
     * @return An Observable that emits a value every second. True if the service is ready to make
     * requests, false if not ready yet
     */
    public Observable<Boolean> isReadyCheck() {
        return Observable.interval(250, TimeUnit.MILLISECONDS).map(new Func1<Long, Boolean>() {
            @Override
            public Boolean call(Long aLong) {
                return mIsServiceReady;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Checks whether the {@link RedditService} instance has been provided with the data needed to
     * authenticate with the reddit API.
     *
     * @return True if the data to authenticate has been provided. False if no data has been
     * provided yet
     */
    public boolean isServiceInitialized() {
        return mIsServiceInitialized;
    }

    /**
     * Checks whether the service is authenticated and able to start making requests
     *
     * @return True if the service is authenticated and waiting to make requests, false if it is not
     * ready yet
     */
    public boolean isServiceReady() {
        return mIsServiceReady;
    }

    public Observable<OauthLoginResult> performLogin(final OauthLoginRequest loginRequest) {
        Timber.v("performLogin() called with: " + "loginRequest = [" + loginRequest + "]");
        OauthLogin oauthLogin = new OauthLogin(getRedditClient());
        return oauthLogin.performLogin(loginRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Makes a request to save/unsave the passed-in contribution
     *
     * @param contribution The contribution to save or unsave
     * @param isSave       True to save, false to unsave
     * @return An Observable that emits the result of the save/unsave API request
     * @throws ServiceNotReadyException When the service isn't ready to make requests yet
     */
    public Observable<SaveResult> saveContribution(
            final PublicContribution contribution, final boolean isSave)
            throws ServiceNotReadyException {
        Timber.v("saveContribution() called with: "
                + "contribution = ["
                + contribution
                + "], isSave = ["
                + isSave
                + "]");
        validateService();
        if (mRedditData == null) throw new ServiceNotReadyException();
        final SaveResult saveResult = new SaveResult(contribution);
        return mRedditData.saveContribution(contribution, isSave).doOnSubscribe(new Action0() {
            @Override
            public void call() {
                Timber.e("saveContribution onSubscribe()");
                authenticate();
            }
        }).map(new Func1<Boolean, SaveResult>() {
            @Override
            public SaveResult call(Boolean aBoolean) {
                saveResult.setSuccessful(true);
                return saveResult;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Sets the OkHttpClient instance to use with all reddit API requests
     *
     * @param okHttpClient The client to use for all requests. A custom client is useful if you want
     *                     to use things such as a proxy or caching for all requests
     */
    public void setOkHttpClient(final OkHttpClient okHttpClient) {
        mOkHttpClient = okHttpClient;
    }

    /**
     * Starts the service, starting the authentication to the reddit API. This is the first method
     * you want to call after getting an instance of {@link RedditService} available. The only time
     * you may want to call this method second is if you want to set a custom OkHttpClient instance
     * for the service to use with its requests via {@link #setOkHttpClient(OkHttpClient)}
     *
     * @param startRequest The request containing all the data needed to authenticate with the
     *                     reddit API
     */
    public void startService(@NonNull StartServiceRequest startRequest) {
        Timber.v("startService() called with: " + "startRequest = [" + startRequest + "]");
        Context serviceContext = this;

        String username = startRequest.getUsername();
        UserAgent userAgent = startRequest.getAppUserAgent();
        String redditClientId = startRequest.getClientId();
        String redditRedirectUrl = startRequest.getRedirectUri();

        if (redditClientId == null || redditRedirectUrl == null) {
            // TODO: Handle null parameters, throw exception
            return;
        }

        mAuthPreferences = new RedditAuthPreferences(serviceContext, username);
        Timber.i(
                "initializeService: Loaded preferences for user [%s]",
                mAuthPreferences.getUsername());
        mServicePreferences = new ServicePreferences(serviceContext);

        mServicePreferences.setRedditClientId(redditClientId);
        mServicePreferences.setRedditRedirectUri(redditRedirectUrl);

        mRedditData = new RedditData(userAgent, mOkHttpClient);

        if (!mRedditData.mRedditClient.isAuthenticated()) {
            Authentication authentication = new Authentication(mRedditData.mRedditClient);
            AuthRequest authRequest = getNewAuthRequest();
            Timber.d("initializeService: authRequest [%s]", authRequest);
            authentication.asyncAuthenticate(authRequest)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(authResultSubscriber());
        }

        mIsServiceInitialized = true;
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
    public Observable<VoteResult> voteContribution(
            final PublicContribution contribution, @VoteRequest.VoteDirection int direction)
            throws ServiceNotReadyException {
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
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        Timber.e("voteContribution onSubscribe()");
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
                .subscribeOn(Schedulers.io())
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
            Timber.e(
                    "getNewAuthRequest: No refresh token found for user [%s]",
                    mAuthPreferences.getUsername());
            return null;
        }
        Timber.v(
                "getNewAuthRequest: Building new auth request for username [%s]",
                mAuthPreferences.getUsername());
        return new AuthRequest(
                mAuthPreferences.getRefreshToken(),
                mServicePreferences.getRedditClientId(),
                mServicePreferences.getRedditRedirectUrl(),
                mAuthPreferences.getAuthenticationJson(),
                mAuthPreferences.getExpirationTime());
    }

    private void handleAuthResult(AuthResult authResult) {
        Timber.v("handleAuthResult() called with: " + "authResult = [" + authResult + "]");
        if (!authResult.isCachedData()) {
            Timber.v("authenticationCallback: Caching new authentication data");
            String authenticationJson = authResult.getAuthenticationJson();
            long currentTime = new Date().getTime();
            long expirationTime = authResult.getExpirationTime() - FIVE_MINUTES_IN_MILLIS;
            Timber.d("handleAuthResult: Crrnt time: [%s]", new Date(currentTime));
            Timber.d("handleAuthResult: Exprn time: [%s]", new Date(expirationTime));
            Timber.d("handleAuthResult: Auth json: [%s]", authenticationJson);
            if (expirationTime > currentTime && !TextUtils.isEmpty(authenticationJson)) {
                mAuthPreferences.setExpirationTime(expirationTime);
                mAuthPreferences.setAuthenticationJson(authenticationJson);
                mAuthPreferences.commit();
                Timber.i(
                        "handleAuthResult: New auth data cached for user [%s]",
                        mAuthPreferences.getUsername());
            } else {
                Timber.i(
                        "handleAuthResult: New auth data was NOT cached for user [%s]",
                        mAuthPreferences.getUsername());
            }
        } else {
            Timber.d(
                    "handleAuthResult: Auth result already cached. Expiration: [%s]",
                    new Date(mAuthPreferences.getExpirationTime()));
        }
        mIsServiceReady = true;
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

