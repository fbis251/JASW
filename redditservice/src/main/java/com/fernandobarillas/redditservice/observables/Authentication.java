package com.fernandobarillas.redditservice.observables;

import android.text.TextUtils;

import com.fernandobarillas.redditservice.requests.AuthRequest;
import com.fernandobarillas.redditservice.results.AuthResult;
import com.orhanobut.logger.Logger;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;

import java.util.Date;
import java.util.UUID;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by fb on 12/15/15.
 */
public class Authentication {
    private static final String LOG_TAG = "Authentication";
    private AuthRequest  mAuthRequest;
    private String       mAuthenticationJson;
    private long         mExpirationTime;
    private RedditClient mRedditClient;

    public Authentication(RedditClient redditClient) {
        Logger.init(LOG_TAG);
        mRedditClient = redditClient;
    }

    /**
     * Performs authentication using an observable
     *
     * @param authRequest The request data to use during authentication
     * @return An Observable that returns an AuthResult
     */
    public Observable<AuthResult> getAuthenticate(final AuthRequest authRequest) {
        mAuthRequest = authRequest;
        return Observable.create(new Observable.OnSubscribe<AuthResult>() {
            @Override
            public void call(Subscriber<? super AuthResult> subscriber) {
                try {
                    subscriber.onNext(getAuthenticate());
                } catch (Exception e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    /**
     * Performs authentication synchronously
     *
     * @param authRequest The request data to use during authentication
     * @return The result of the authentication procedure
     * @throws OAuthException When an error occurs during the OAuth procedure
     */
    public AuthResult authenticate(final AuthRequest authRequest) throws OAuthException {
        mAuthRequest = authRequest;
        return getAuthenticate();
    }

    private AuthResult getAuthenticate() throws OAuthException {
        String      refreshToken       = mAuthRequest.getRefreshToken();
        String      redditClientId     = mAuthRequest.getRedditClientId();
        String      redditRedirectUrl  = mAuthRequest.getRedditRedirectUrl();
        String      authenticationJson = mAuthRequest.getAuthenticationJson();
        long        expirationTime     = mAuthRequest.getExpirationTime();
        OAuthHelper oAuthHelper        = mRedditClient.getOAuthHelper();
        OAuthData   oAuthData;
        Credentials credentials;
        long        currentTime        = new Date().getTime();
        boolean     expired            = expirationTime < currentTime;
        if (!TextUtils.isEmpty(refreshToken)) {
            // A user refresh token is stored
            Logger.i("getAuthenticate: Using refresh token to getAuthenticate");
            credentials = Credentials.installedApp(redditClientId, redditRedirectUrl);
            oAuthHelper.setRefreshToken(refreshToken);
            if (!expired && !TextUtils.isEmpty(authenticationJson)) {
                Logger.d("getAuthenticate: Using cached authentication data");
                oAuthData = oAuthHelper.refreshToken(credentials, authenticationJson);
            } else {
                Logger.d("getAuthenticate: Requesting new authentication data");
                oAuthData = oAuthHelper.refreshToken(credentials);
                mExpirationTime = oAuthData.getExpirationDate()
                        .getTime();
                mAuthenticationJson = oAuthData.getDataNode()
                        .toString();
            }
        } else {
            // We can't perform a user login with no refresh token, this means that the
            // user hasn't tried to log in yet.
            Logger.i("getAuthenticate: Performing app-only authentication");
            UUID deviceUuid = new UUID(0, 0);
            credentials = Credentials.userlessApp(redditClientId, deviceUuid);
            if (!expired) {
                Logger.d("getAuthenticate: Using cached authentication data");
                oAuthData = mRedditClient.getOAuthHelper()
                        .easyAuth(credentials, authenticationJson);
            } else {
                Logger.d("getAuthenticate: Requesting new authentication data");
                oAuthData = mRedditClient.getOAuthHelper()
                        .easyAuth(credentials);
            }
            mExpirationTime = oAuthData.getExpirationDate()
                    .getTime();
            mAuthenticationJson = oAuthData.getDataNode()
                    .toString();
            Logger.json(mAuthenticationJson);
        }

        mRedditClient.authenticate(oAuthData);
        return new AuthResult(mRedditClient.isAuthenticated(),
                              mAuthenticationJson,
                              mExpirationTime);
    }
}
