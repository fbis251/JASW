package com.fernandobarillas.redditservice.observables;

import android.text.TextUtils;

import com.fernandobarillas.redditservice.requests.AuthRequest;
import com.fernandobarillas.redditservice.results.AuthResult;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;

import java.util.Date;
import java.util.UUID;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

/**
 * Created by fb on 12/15/15.
 */
public class Authentication {
    private AuthRequest  mAuthRequest;
    private String       mAuthenticationJson;
    private long         mExpirationTime;
    private RedditClient mRedditClient;

    public Authentication(RedditClient redditClient) {
        mRedditClient = redditClient;
    }

    /**
     * Checks whether the passed-in expiration time has expired.
     *
     * @param expirationTime The timestamp to check for expiration
     * @return True if the timestamp is in the past (not expired), false if the timestamp is in the
     * future (expired)
     */
    public static boolean isExpired(long expirationTime) {
        long currentTime = new Date().getTime();
        return expirationTime < currentTime;
    }

    /**
     * Performs authentication asynchronously using an observable
     *
     * @param authRequest The request data to use during authentication
     * @return An Observable that returns an AuthResult
     */
    public Observable<AuthResult> asyncAuthenticate(final AuthRequest authRequest) {
        Timber.v("asyncAuthenticate() called with: " + "authRequest = [" + authRequest + "]");
        mAuthRequest = authRequest;
        return Observable.create(new Observable.OnSubscribe<AuthResult>() {
            @Override
            public void call(Subscriber<? super AuthResult> subscriber) {
                try {
                    subscriber.onNext(performAuthentication());
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
        Timber.v("authenticate() called with: " + "authRequest = [" + authRequest + "]");
        mAuthRequest = authRequest;
        return performAuthentication();
    }

    private AuthResult performAuthentication() throws OAuthException, NetworkException {
        Timber.v("performAuthentication() called");
        String refreshToken = mAuthRequest.getRefreshToken();
        String redditClientId = mAuthRequest.getRedditClientId();
        String redditRedirectUrl = mAuthRequest.getRedditRedirectUrl();
        String authenticationJson = mAuthRequest.getAuthenticationJson();
        long expirationTime = mAuthRequest.getExpirationTime();
        OAuthHelper oAuthHelper = mRedditClient.getOAuthHelper();
        OAuthData oAuthData;
        Credentials credentials;
        boolean isExpired = isExpired(expirationTime);
        boolean isCachedData = false; // True when cached data was used to authenticate
        if (!TextUtils.isEmpty(refreshToken)) {
            // A user refresh token is stored
            Timber.i("performAuthentication: Using refresh token to authenticate");
            credentials = Credentials.installedApp(redditClientId, redditRedirectUrl);
            oAuthHelper.setRefreshToken(refreshToken);
            if (!isExpired && !TextUtils.isEmpty(authenticationJson)) {
                Timber.d("performAuthentication: Using cached authentication data");
                oAuthData = oAuthHelper.refreshToken(credentials, authenticationJson);
                isCachedData = true;
            } else {
                Timber.d("performAuthentication: Requesting new authentication data");
                oAuthData = oAuthHelper.refreshToken(credentials);
                mExpirationTime = oAuthData.getExpirationDate().getTime();
                mAuthenticationJson = oAuthData.getDataNode().toString();
            }
        } else {
            // We can't perform a user login with no refresh token, this means that the
            // user hasn't tried to log in yet.
            Timber.i("performAuthentication: Performing app-only authentication");
            UUID deviceUuid = new UUID(0, 0);
            credentials = Credentials.userlessApp(redditClientId, deviceUuid);
            if (!isExpired) {
                Timber.d("performAuthentication: Using cached authentication data");
                oAuthData =
                        mRedditClient.getOAuthHelper().easyAuth(credentials, authenticationJson);
                isCachedData = true;
            } else {
                Timber.d("performAuthentication: Requesting new authentication data");
                oAuthData = mRedditClient.getOAuthHelper().easyAuth(credentials);
            }
            mExpirationTime = oAuthData.getExpirationDate().getTime();
            mAuthenticationJson = oAuthData.getDataNode().toString();
            Timber.d(mAuthenticationJson);
        }

        // Make the authentication network request
        mRedditClient.authenticate(oAuthData);
        return new AuthResult(mRedditClient.isAuthenticated(), mAuthenticationJson, mExpirationTime,
                isCachedData);
    }
}
