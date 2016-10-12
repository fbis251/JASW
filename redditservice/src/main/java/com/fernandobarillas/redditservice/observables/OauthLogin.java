package com.fernandobarillas.redditservice.observables;

import com.fernandobarillas.redditservice.requests.OauthLoginRequest;
import com.fernandobarillas.redditservice.results.OauthLoginResult;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

/**
 * Created by fb on 8/3/16.
 */
public class OauthLogin {
    private RedditClient mRedditClient;

    public OauthLogin(RedditClient redditClient) {
        mRedditClient = redditClient;
    }

    public Observable<OauthLoginResult> performLogin(final OauthLoginRequest loginRequest) {
        return Observable.create(new Observable.OnSubscribe<OauthLoginResult>() {
            @Override
            public void call(Subscriber<? super OauthLoginResult> subscriber) {
                Timber.v("performLogin call() called with: " + "subscriber = [" + subscriber + "]");
                try {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(getLoginResult(loginRequest));
                    }
                    if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
                } catch (Exception e) {
                    if (!subscriber.isUnsubscribed()) subscriber.onError(e);
                }
            }
        });
    }

    private OauthLoginResult getLoginResult(final OauthLoginRequest loginRequest)
            throws OAuthException {
        Timber.v("getLoginResult() called with: " + "loginRequest = [" + loginRequest + "]");
        OAuthHelper oAuthHelper = mRedditClient.getOAuthHelper();
        String authorizationUrl = loginRequest.getAuthorizationUrl();
        Credentials credentials = loginRequest.getCredentials();
        OAuthData oAuthData = oAuthHelper.onUserChallenge(authorizationUrl, credentials);
        mRedditClient.authenticate(oAuthData);
        String username = mRedditClient.me().getFullName();
        String refreshToken = mRedditClient.getOAuthData().getRefreshToken();
        return new OauthLoginResult(username, refreshToken, oAuthData);
    }
}
