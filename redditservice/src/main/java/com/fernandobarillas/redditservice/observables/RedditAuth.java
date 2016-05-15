package com.fernandobarillas.redditservice.observables;

import android.text.TextUtils;
import android.util.Log;

import com.fernandobarillas.redditservice.exceptions.AuthenticationException;
import com.fernandobarillas.redditservice.requests.AuthRequest;
import com.fernandobarillas.redditservice.results.AuthResult;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;

import java.util.Calendar;
import java.util.UUID;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by fb on 12/15/15.
 */
public class RedditAuth {
    private static final String LOG_TAG = "RedditAuth";
    private AuthRequest  mAuthRequest;
    private String       mAuthenticatedUsername;
    private String       mAuthenticationJson;
    private long         mExpirationTime;
    private RedditClient mRedditClient;

    public RedditAuth(RedditClient redditClient) {
        mRedditClient = redditClient;
    }

    public Observable<AuthResult> authenticate(final AuthRequest authRequest) {
        mAuthRequest = authRequest;
        return Observable.create(new Observable.OnSubscribe<AuthResult>() {
            @Override
            public void call(Subscriber<? super AuthResult> subscriber) {
                try {
                    subscriber.onNext(authenticate());
                } catch (OAuthException | AuthenticationException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    private AuthResult authenticate() throws OAuthException, AuthenticationException {
        String      refreshToken       = mAuthRequest.getRefreshToken();
        String      redditClientId     = mAuthRequest.getRedditClientId();
        String      redditRedirectUrl  = mAuthRequest.getRedditRedirectUrl();
        String      authenticationJson = mAuthRequest.getAuthenticationJson();
        long        expirationTime     = mAuthRequest.getExpirationTime();
        Credentials credentials;
        OAuthHelper oAuthHelper        = mRedditClient.getOAuthHelper();
        OAuthData   oAuthData;
        if (!refreshToken.isEmpty()) {
            // A user refresh token is stored
            Log.i(LOG_TAG, "doInBackground: Using refresh token to authenticate");
            credentials = Credentials.installedApp(redditClientId, redditRedirectUrl);
            oAuthHelper.setRefreshToken(refreshToken);
            long    currentTime = Calendar.getInstance().getTimeInMillis();
            boolean expired     = expirationTime < currentTime;
            long    timeLeft    = expirationTime - currentTime;
            Log.v(LOG_TAG, "doInBackground: Cached time:  " + expirationTime);
            Log.v(LOG_TAG, "doInBackground: Current time: " + currentTime);
            Log.v(LOG_TAG, "doInBackground: Time left: " + timeLeft);
            Log.v(LOG_TAG, "doInBackground: Expired? : " + expired);
            if (!expired && !TextUtils.isEmpty(authenticationJson)) {
                Log.v(LOG_TAG, "doInBackground: Using cached authentication data");
                oAuthData = oAuthHelper.refreshToken(credentials, authenticationJson);
            } else {
                Log.v(LOG_TAG, "doInBackground: Requesting new authentication data");
                oAuthData = oAuthHelper.refreshToken(credentials);
            }
        } else {
            // We can't perform a user login with no refresh token, this means that the
            // user hasn't tried to log in yet.
            Log.i(LOG_TAG, "doInBackground: Performing app-only authentication");
            UUID deviceUuid = new UUID(0, 0);
            credentials = Credentials.userlessApp(redditClientId, deviceUuid);
            oAuthData = mRedditClient.getOAuthHelper().easyAuth(credentials);
        }
        mRedditClient.authenticate(oAuthData);
        // Pass back the authentication data to the caller in order to cache it for
        // later use
        mExpirationTime = oAuthData.getExpirationDate().getTime();
        mAuthenticationJson = oAuthData.getDataNode().toString();
        if (mRedditClient.isAuthenticated()) {
            // TODO: get this elsewhere so it doesn't create a new blocking HTTP request
//            mAuthenticatedUsername = mRedditClient.getAuthenticatedUser();

            return new AuthResult(mRedditClient.isAuthenticated(),
                                  mAuthenticatedUsername,
                                  mAuthenticationJson,
                                  mExpirationTime);
        } else {
            throw new AuthenticationException();
        }
    }
}
