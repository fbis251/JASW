package com.fernandobarillas.redditservice.tasks;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.fernandobarillas.redditservice.exceptions.NullRedditClientException;
import com.fernandobarillas.redditservice.requests.AuthenticationRequest;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthHelper;

import java.util.Calendar;
import java.util.UUID;

/**
 * Created by fb on 12/15/15.
 */
public class AuthenticationTask extends AsyncTask<AuthenticationRequest, Void, Exception> {
    private static final String LOG_TAG = "AuthenticationTask";
    private AuthenticationRequest mAuthenticationRequest;
    private String                mAuthenticatedUsername;
    private String                mAuthenticationJson;
    private long                  mExpirationTime;

    @Override
    protected Exception doInBackground(AuthenticationRequest... authenticationRequests) {
        Log.d(LOG_TAG, "doInBackground()");
        if (authenticationRequests.length != 1) {
            // TODO: Create custom Exception
            return new Exception("More than 1 login request passed in");
        }

        mAuthenticationRequest = authenticationRequests[0];
        RedditClient redditClient = mAuthenticationRequest.getRedditClient();

        if (redditClient == null) {
            return new NullRedditClientException();
        }

        String      refreshToken       = mAuthenticationRequest.getRefreshToken();
        String      redditClientId     = mAuthenticationRequest.getRedditClientId();
        String      redditRedirectUrl  = mAuthenticationRequest.getRedditRedirectUrl();
        String      authenticationJson = mAuthenticationRequest.getAuthenticationJson();
        long        expirationTime     = mAuthenticationRequest.getExpirationTime();
        Credentials credentials;
        OAuthHelper oAuthHelper        = redditClient.getOAuthHelper();
        OAuthData   oAuthData;
        try {
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
                oAuthData = redditClient.getOAuthHelper().easyAuth(credentials);
            }

            redditClient.authenticate(oAuthData);

            // Pass back the authentication data to the caller in order to cache it for later use
            mExpirationTime = oAuthData.getExpirationDate().getTime();
            mAuthenticationJson = oAuthData.getDataNode().toString();
            if (redditClient.isAuthenticated()) {
                // TODO: get this elsewhere so it doesn't create a new blocking HTTP request
//                mAuthenticatedUsername = redditClient.getAuthenticatedUser();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "doInBackground: Unable to authenticate", e);
            return e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Exception e) {
        Log.d(LOG_TAG, "onPostExecute() called with: " + "e = [" + e + "]");
        super.onPostExecute(e);

        // Make sure a onComplete has been set
        if (mAuthenticationRequest.getAuthenticationCallback() == null) {
            return;
        }

        // Now that we know the onComplete isn't null, execute it
        mAuthenticationRequest.getAuthenticationCallback()
                .authenticationCallback(mAuthenticatedUsername, mAuthenticationJson, mExpirationTime, e);
    }
}
