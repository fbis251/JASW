package com.fernandobarillas.redditservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.fernandobarillas.redditservice.callbacks.RedditLinksCallback;
import com.fernandobarillas.redditservice.data.RedditData;
import com.fernandobarillas.redditservice.models.Link;
import com.fernandobarillas.redditservice.observables.Authentication;
import com.fernandobarillas.redditservice.preferences.RedditAuthPreferences;
import com.fernandobarillas.redditservice.preferences.ServicePreferences;
import com.fernandobarillas.redditservice.requests.AuthRequest;
import com.fernandobarillas.redditservice.requests.SubredditRequest;
import com.fernandobarillas.redditservice.results.AuthResult;
import com.fernandobarillas.redditservice.results.SaveResult;
import com.fernandobarillas.redditservice.results.VoteResult;
import com.orhanobut.logger.Logger;

import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.VoteDirection;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RedditService extends Service {
    private static final String LOG_TAG                 = "RedditService";
    public static final  String USERNAME_KEY            = "username";
    public static final  String USER_AGENT_KEY          = "user_agent";
    public static final  String REDDIT_CLIENT_ID_KEY    = "reddit_client_id";
    public static final  String REDDIT_REDIRECT_URL_KEY = "reddit_redirect_url";

    private final IBinder mIBinder = new RedditBinder();
    private RedditAuthPreferences mAuthPreferences;
    private ServicePreferences    mServicePreferences;
    private RedditData            mRedditData;
    private boolean mIsServiceReady = false; // Ready to provide data

    public RedditService() {
        Logger.init(LOG_TAG)
                .hideThreadInfo();
        Logger.d("RedditService() instantiated");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.v("onStartCommand() called with: " + "intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        initializeService(intent);

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Logger.v("onDestroy() called");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.d("onBind() called with: " + "intent = [" + intent + "]");
        initializeService(intent);

        return mIBinder;
    }

    public static Intent getRedditServiceIntent(Context context,
                                                String username,
                                                String clientId,
                                                String redirectUrl,
                                                UserAgent appUserAgent) {
        Logger.init(LOG_TAG);
        Logger.v("getRedditServiceIntent()");

        // Bind the reddit service to this Activity
        Intent redditServiceIntent = new Intent(context, RedditService.class);
        redditServiceIntent.putExtra(RedditService.USERNAME_KEY, username);
        redditServiceIntent.putExtra(RedditService.REDDIT_CLIENT_ID_KEY, clientId);
        redditServiceIntent.putExtra(RedditService.REDDIT_REDIRECT_URL_KEY, redirectUrl);
        redditServiceIntent.putExtra(RedditService.USER_AGENT_KEY, appUserAgent.toString());
        return redditServiceIntent;
    }

    public int getLastViewedLink() {
        return mRedditData.getLastViewedLink();
    }

    public void setLastViewedLink(int lastViewedLink) {
        mRedditData.setLastViewedLink(lastViewedLink);
    }

    public Link getLink(int whichLink) {
        return mRedditData.getLink(whichLink);
    }

    public int getLinkCount() {
        return mRedditData.getLinkCount();
    }

    public void getMoreLinks(final RedditLinksCallback linksCallback) {
        mRedditData.getMoreLinks(linksCallback);
    }

    public void getNewLinks(final SubredditRequest subredditRequest) {
        Logger.v("getNewLinks() called with: " + "subredditRequest = [" + subredditRequest + "]");
        mRedditData.getNewLinks(subredditRequest);
    }

    public int getNsfwImageCount() {
        return mRedditData.getNsfwImageCount();
    }

    public boolean isServiceReady() {
        return mIsServiceReady;
    }

    public Observable<SaveResult> saveLink(final Link link, final boolean isSave) {
        final SaveResult saveResult = new SaveResult(link);
        return mRedditData.saveLink(link, isSave)
                .subscribeOn(Schedulers.io())
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

    public Observable<VoteResult> voteLink(final Link link, final VoteDirection voteDirection) {
        final VoteResult voteResult = new VoteResult(link);
        return mRedditData.voteLink(link, voteDirection)
                .subscribeOn(Schedulers.io())
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

    private void asyncAuthenticate() {
        AuthRequest request = getNewAuthRequest();

        Authentication authentication = new Authentication(mRedditData.mRedditClient);
        authentication.getAuthenticate(request)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<AuthResult>() {

                    @Override
                    public void onCompleted() {
                        Logger.v("asyncAuthenticate onCompleted() called");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "asyncAuthenticate");
                        // TODO: let the client know that authentication failed
                    }

                    @Override
                    public void onNext(AuthResult authResult) {
                        Logger.v("asyncAuthenticate onNext() called with: " + "authResult = [" +
                                         authResult + "]");
                        handleAuthResult(authResult);
                    }
                });
    }

    private void authenticate() {
        AuthRequest request = getNewAuthRequest();

        Authentication authentication = new Authentication(mRedditData.mRedditClient);
        try {
            handleAuthResult(authentication.authenticate(request));
        } catch (OAuthException e) {
            Logger.e(e, "authenticate() failed");
        }
    }

    private AuthRequest getNewAuthRequest() {
        if (mAuthPreferences.getUsername() != null && mAuthPreferences.getRefreshToken() == null) {
            // TODO: Handle non-userless with no refresh token
            return null;
        }

        return new AuthRequest(mAuthPreferences.getRefreshToken(),
                               mServicePreferences.getRedditClientId(),
                               mServicePreferences.getRedditRedirectUrl(),
                               mAuthPreferences.getAuthenticationJson(),
                               mAuthPreferences.getExpirationTime());
    }

    private void handleAuthResult(AuthResult authResult) {
        Logger.v("handleAuthResult() called with: " + "authResult = [" + authResult + "]");
        String authenticationJson = authResult.getAuthenticationJson();
        long   expirationTime     = authResult.getExpirationTime();
        Logger.v("authenticationCallback: Caching authentication data");
        // Cache the authentication data
        if (expirationTime != AuthRequest.INVALID_EXPIRATION_TIME) {
            mAuthPreferences.setExpirationTime(expirationTime);
        }
        if (!TextUtils.isEmpty(authenticationJson)) {
            mAuthPreferences.setAuthenticationJson(authenticationJson);
        }
        mIsServiceReady = true;
    }

    private void initializeService(Intent intent) {
        Logger.v("initializeService() called with: " + "intent = [" + intent + "]");

        Context serviceContext = this;

        String username = intent.getExtras()
                .getString(USERNAME_KEY, null);
        String userAgentString = intent.getExtras()
                .getString(USER_AGENT_KEY, Constants.DEFAULT_USER_AGENT);
        String redditClientId = intent.getExtras()
                .getString(REDDIT_CLIENT_ID_KEY);
        String redditRedirectUrl = intent.getExtras()
                .getString(REDDIT_REDIRECT_URL_KEY);

        mAuthPreferences = new RedditAuthPreferences(serviceContext, username);
        mServicePreferences = new ServicePreferences(serviceContext);

        mServicePreferences.setUserAgentString(userAgentString);
        mServicePreferences.setRedditClientId(redditClientId);
        mServicePreferences.setRedditRedirectUrl(redditRedirectUrl);

        // TODO: Handle null extras
        mRedditData = RedditData.getInstance(UserAgent.of(mServicePreferences.getUserAgentString()));

        asyncAuthenticate();
    }

    public class RedditBinder extends Binder {
        public RedditService getService() {
            Logger.d("getService()");
            return RedditService.this;
        }
    }
}
