package test.fb.servicetest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fernandobarillas.redditservice.RedditService;
import com.fernandobarillas.redditservice.requests.SubredditRequest;

import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.TimePeriod;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    /** Seconds between UI updates from RedditService */
    private static final int UPDATE_INTERVAL = 1;

    private RedditService        mRedditService;
    private TextView             mElapsedTimeTextView;
    private Button               mGetMoreLinksButton;
    private FloatingActionButton mFab;
    private ServiceConnection    mRedditServiceConnection;
    private Subscription         mWaitForServiceSubscription;
    private SubredditRequest     mSubredditRequest;
    private SubredditPaginator   mPaginator;
    private Set<Submission>      mSubmissionSet;

    private Date mStartDate;
    private Date mReadyDate;

    public MainActivity() {
        Timber.v("MainActivity() called");
        mRedditServiceConnection = new MainConnection();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.v("onCreate() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        mStartDate = new Date();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mElapsedTimeTextView = (TextView) findViewById(R.id.elapsed_time);
        mGetMoreLinksButton = (Button) findViewById(R.id.get_links_button);
        mFab = (FloatingActionButton) findViewById(R.id.fab);

        setSupportActionBar(toolbar);
        mGetMoreLinksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Timber.v("onClick() called with: " + "view = [" + view + "]");
                if (mRedditService == null || mPaginator == null) return;
                mRedditService.getMoreSubmissions(mPaginator)
                        .toList()
                        .subscribe(submissionsHandler());
            }
        });
        updateElapsedTimeTextView();

        mSubmissionSet = new LinkedHashSet<>();
        mSubredditRequest = new SubredditRequest.Builder("all").setLinkLimit(100)
                .setSorting(Sorting.TOP)
                .setTimePeriod(TimePeriod.WEEK)
                .build();
    }

    @Override
    protected void onStop() {
        Timber.v("onStop() called");
        stopService(new Intent(this, RedditService.class));
        super.onStop();
    }

    @Override
    protected void onPause() {
        Timber.v("onPause() called");
        unbindService(mRedditServiceConnection);
        super.onPause();
    }

    @Override
    protected void onResume() {
        Timber.v("onResume() called");
        super.onResume();
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String platform = "Android";
        String appId = getApplication().getPackageName();
        String version = (packageInfo != null) ? packageInfo.versionName : "";
        String creatorUsername = "fbis251";
        UserAgent appUserAgent = UserAgent.of(platform, appId, version, creatorUsername);

        boolean userless = true; // Set to true to not do a client-only authentication
        String username = userless ? null : PrivateConstants.USERNAME;

        Intent redditServiceIntent =
                RedditService.getRedditServiceIntent(getApplicationContext(), username,
                        PrivateConstants.REDDIT_CLIENT_ID, PrivateConstants.REDDIT_REDIRECT_URL,
                        appUserAgent);
        bindService(redditServiceIntent, mRedditServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private Subscriber<List<Submission>> submissionsHandler() {
        Timber.v("submissionsHandler() called");
        return new Subscriber<List<Submission>>() {
            @Override
            public void onCompleted() {
                Timber.v("submissionsHandler onCompleted() called");
                if (mSubmissionSet == null) return;
                Timber.i("submissionsHandler onCompleted: Total submissions: [%d]",
                        mSubmissionSet.size());
            }

            @Override
            public void onStart() {
                Timber.v("submissionsHandler onStart() called");
                super.onStart();
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "submissionsHandler onError: ");
            }

            @Override
            public void onNext(List<Submission> submissions) {
                Timber.i("submissionsHandler onNext: Got [%d] submissions", submissions.size());
                if (mSubmissionSet == null) return;
                mSubmissionSet.addAll(submissions);
            }
        };
    }

    private Subscriber<List<Subreddit>> subscriptionsHandler() {
        Timber.v("subscriptionsHandler() called");
        return new Subscriber<List<Subreddit>>() {
            @Override
            public void onCompleted() {
                Timber.v("subscriptionsHandler onCompleted() called");
            }

            @Override
            public void onStart() {
                Timber.v("subscriptionsHandler onStart() called");
                super.onStart();
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "subscriptionsHandler onError: ");
            }

            @Override
            public void onNext(List<Subreddit> subreddits) {
                Timber.i("submissionsHandler onNext: Got [%d] subreddits", subreddits.size());
            }
        };
    }

    private void updateElapsedTimeTextView() {
        Timber.v("updateElapsedTimeTextView() called");
        final long startTime = new Date().getTime();
        Observable.interval(UPDATE_INTERVAL, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Long, Object>() {
                    @Override
                    public Object call(Long aLong) {
                        if (mElapsedTimeTextView == null || mRedditService == null) return null;

                        boolean isReady = mRedditService.isServiceReady();
                        long elapsedTime = (new Date().getTime() - startTime) / 1000;
                        int linkCount = mSubmissionSet != null ? mSubmissionSet.size() : 0;

                        String serviceStatus = (isReady) ? "Ready" : "NOT READY";
                        Date readyDate = mReadyDate != null ? mReadyDate : new Date();
                        double serviceWaitTime =
                                (readyDate.getTime() - mStartDate.getTime()) / 1000.0;
                        mElapsedTimeTextView.setText(String.format(
                                "Running for %d second(s)\n%s\nService Wait Time: %.2fs\nLink Count: %d",
                                elapsedTime, serviceStatus, serviceWaitTime, linkCount));
                        return null;
                    }
                })
                .subscribe();
    }

    private void waitForServiceAuthentication() {
        Timber.v("waitForServiceAuthentication() called");
        if (mRedditService == null) return;
        mRedditService.isReadyCheck().takeUntil(new Func1<Boolean, Boolean>() {
            @Override
            public Boolean call(Boolean isServiceReady) {
                // Stop emitting items once the service is ready so that the subscribers'
                // onCompleted method can handle the service being ready
                return isServiceReady;
            }
        }).subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {
                Timber.v("waitForServiceAuthentication onCompleted() called");
                mReadyDate = new Date();
                long waitTime = (mReadyDate.getTime() - mStartDate.getTime()) / 1000;
                Timber.i("waitForService() Service is ready, waited for %d seconds", waitTime);

                // Download the subreddit data you requested with mSubredditRequest
                mPaginator = mRedditService.getSubredditPaginator(mSubredditRequest);
                mRedditService.getMoreSubmissions(mPaginator)
                        .toList()
                        .subscribe(submissionsHandler());
                // Download the logged-in user's subreddit subscriptions
                mRedditService.getSubscriptions().subscribe(subscriptionsHandler());
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e,
                        "waitForServiceAuthentication onError() called with: " + "e = [" + e + "]");
            }

            @Override
            public void onNext(Boolean isServiceAuthenticated) {
                Timber.v("waitForServiceAuthentication onNext() called with: "
                        + "isServiceAuthenticated = ["
                        + isServiceAuthenticated
                        + "]");
            }
        });
    }

    private class MainConnection implements ServiceConnection {

        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            Timber.v("onServiceConnected() called with: "
                    + "className = ["
                    + className
                    + "], iBinder = ["
                    + iBinder
                    + "]");
            RedditService.RedditBinder redditBinder = (RedditService.RedditBinder) iBinder;
            mRedditService = redditBinder.getService();
            if (mRedditService == null) {
                return;
                // TODO: Handle service unavailable, perform retries
            }

            // Now you can begin waiting for the service to authenticate with the reddit API
            waitForServiceAuthentication();
        }

        public void onServiceDisconnected(ComponentName className) {
            Timber.v("onServiceDisconnected() called with: " + "className = [" + className + "]");
            mRedditService = null;
        }
    }
}
