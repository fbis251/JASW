package com.fernandobarillas.redditservice.tasks;

import com.fernandobarillas.redditservice.exceptions.NullRedditClientException;
import com.fernandobarillas.redditservice.requests.AuthenticationRequest;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthHelper;

import android.os.AsyncTask;
import android.util.Log;

import java.util.UUID;

/**
 * Created by fb on 12/15/15.
 */
public class AuthenticationTask extends AsyncTask<AuthenticationRequest, Void, Exception> {
    private static final String LOG_TAG = "AuthenticationTask";
    private AuthenticationRequest mAuthenticationRequest;

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

        String refreshToken = mAuthenticationRequest.getRefreshToken();
        String redditClientId = mAuthenticationRequest.getRedditClientId();
        String redditRedirectUrl = mAuthenticationRequest.getRedditRedirectUrl();
        Credentials credentials;
        OAuthHelper oAuthHelper = redditClient.getOAuthHelper();
        OAuthData oAuthData;
        try {
            if (!refreshToken.isEmpty()) {
                // A user refresh token is stored
                Log.i(LOG_TAG, "doInBackground: Using refresh token to authenticate");
                credentials = Credentials.installedApp(redditClientId, redditRedirectUrl);
                oAuthHelper.setRefreshToken(refreshToken);
                oAuthData = oAuthHelper.refreshToken(credentials);
            } else {
                // We can't perform a user login with no refresh token, this means that the
                // user hasn't tried to log in yet.
                Log.i(LOG_TAG, "doInBackground: Performing app-only authentication");
                UUID deviceUuid = new UUID(0, 0);
                credentials = Credentials.userlessApp(redditClientId, deviceUuid);
                oAuthData = redditClient.getOAuthHelper().easyAuth(credentials);
            }

            redditClient.authenticate(oAuthData);
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

        // Make sure a callback has been set
        if (mAuthenticationRequest.getAuthenticationCallback() == null) {
            return;
        }

        // Now that we know the callback isn't null, execute it
        mAuthenticationRequest.getAuthenticationCallback().authenticationCallback(e);
    }
}
