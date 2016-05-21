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
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fernandobarillas.redditservice.RedditService;
import com.fernandobarillas.redditservice.callbacks.RedditLinksCallback;
import com.fernandobarillas.redditservice.models.Link;
import com.fernandobarillas.redditservice.requests.SubredditRequest;
import com.fernandobarillas.redditservice.results.VoteResult;
import com.orhanobut.logger.Logger;

import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG         = "MainActivity";
    private static final int    UPDATE_INTERVAL = 1; // Seconds between UI updates from RedditService

    private RedditService        mRedditService;
    private TextView             mElapsedTimeTextView;
    private Button               mGetMoreLinksButton;
    private FloatingActionButton mFab;
    private ServiceConnection    mRedditServiceConnection;
    private Subscription         mWaitForServiceSubscription;

    public MainActivity() {
        Logger.init(LOG_TAG);
        mRedditServiceConnection = new MainConnection();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.v("onCreate() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mElapsedTimeTextView = (TextView) findViewById(R.id.elapsed_time);
        mGetMoreLinksButton = (Button) findViewById(R.id.get_links_button);
        mFab = (FloatingActionButton) findViewById(R.id.fab);

        setSupportActionBar(toolbar);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (mRedditService == null) return;
                final Link link = mRedditService.getLink(mRedditService.getLinkCount() - 1);
                if (link == null) return;
                mRedditService.voteLink(link, VoteDirection.UPVOTE)
                        .subscribe(new Action1<VoteResult>() {
                            @Override
                            public void call(VoteResult voteResult) {
                                if (voteResult.getThrowable() != null) {
                                    Snackbar.make(view,
                                                  "Error when voting: " + voteResult.getThrowable()
                                                          .getLocalizedMessage(),
                                                  Snackbar.LENGTH_INDEFINITE)
                                            .show();
                                } else {
                                    Snackbar.make(view,
                                                  "Voted " + link.getTitle(),
                                                  Snackbar.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        });
            }
        });
        mGetMoreLinksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (mRedditService == null) return;
                mRedditService.getMoreLinks(handleRedditDataUpdate(mGetMoreLinksButton));
            }
        });
        updateElapsedTimeTextView();
    }

    @Override
    protected void onStop() {
        Logger.v("onStop() called");
        stopService(new Intent(this, RedditService.class));
        super.onStop();
    }

    @Override
    protected void onPause() {
        Logger.v("onPause() called");
        unbindService(mRedditServiceConnection);
        super.onPause();
    }

    @Override
    protected void onResume() {
        Logger.v("onResume() called");
        super.onResume();
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String    platform        = "Android";
        String    appId           = getApplication().getPackageName();
        String    version         = (packageInfo != null) ? packageInfo.versionName : "";
        String    creatorUsername = "fbis251";
        UserAgent appUserAgent    = UserAgent.of(platform, appId, version, creatorUsername);

        Intent redditServiceIntent = RedditService.getRedditServiceIntent(getApplicationContext(),
                                                                          PrivateConstants.USERNAME,
                                                                          PrivateConstants.REDDIT_CLIENT_ID,
                                                                          PrivateConstants.REDDIT_REDIRECT_URL,
                                                                          appUserAgent);
        bindService(redditServiceIntent, mRedditServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private RedditLinksCallback handleRedditDataUpdate(final View view) {
        Logger.v("handleRedditDataUpdate() called");
        return new RedditLinksCallback() {
            @Override
            public void linksCallback(Exception e) {
                if (e != null) {
                    Logger.e(e, "linksCallback()");
                    return;
                }
                if (view != null) {
                    Snackbar.make(view, "Downloaded more links", Snackbar.LENGTH_LONG)
                            .setAction("Action", null)
                            .show();
                }
            }
        };
    }

    private void stopWaiting() {
        Logger.v("stopWaiting() called");
        if (mWaitForServiceSubscription != null) mWaitForServiceSubscription.unsubscribe();
    }

    private void updateElapsedTimeTextView() {
        Logger.v("updateElapsedTimeTextView() called");
        final long startTime = new Date().getTime();
        Observable.interval(UPDATE_INTERVAL, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Long, Object>() {
                    @Override
                    public Object call(Long aLong) {
                        if (mElapsedTimeTextView == null || mRedditService == null) return null;

                        boolean isReady     = mRedditService.isServiceReady();
                        long    elapsedTime = (new Date().getTime() - startTime) / 1000;
                        int     linkCount   = mRedditService.getLinkCount();

                        mElapsedTimeTextView.setText(String.format(
                                "%d second(s)\n%s\nLink Count: %d",
                                elapsedTime,
                                (isReady) ? "Ready" : "NOT READY",
                                linkCount));
                        return null;
                    }
                })
                .subscribe();
    }

    private void waitForService() {
        mWaitForServiceSubscription = Observable.interval(UPDATE_INTERVAL, TimeUnit.SECONDS)
                .map(new Func1<Long, Object>() {
                    @Override
                    public Object call(Long aLong) {
                        if (mRedditService == null) return null;
                        if (!mRedditService.isServiceReady()) {
                            Logger.d("waitForService() Service not ready yet, waiting");
                            return null;
                        }
                        Logger.i("waitForService() Service is ready, performing request");
                        SubredditRequest subredditRequest = new SubredditRequest.Builder("all").setLinkLimit(
                                100)
                                .setSorting(Sorting.TOP)
                                .setTimePeriod(TimePeriod.YEAR)
                                .setLinkValidator(new TestValidator())
                                .setRedditLinksCallback(handleRedditDataUpdate(mElapsedTimeTextView))
                                .build();
                        mRedditService.getNewLinks(subredditRequest);
                        stopWaiting();
                        return null;
                    }
                })
                .subscribe();
    }

    private class MainConnection implements ServiceConnection {
        private static final String LOG_TAG = "ServiceConnection";

        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            Logger.v("onServiceConnected() called with: " + "className = [" + className + "], iBinder = [" + iBinder + "]");
            RedditService.RedditBinder redditBinder = (RedditService.RedditBinder) iBinder;
            mRedditService = redditBinder.getService();
            if (mRedditService == null) {
                return;
                // TODO: Handle service unavailable, perform retries
            }

            waitForService();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(LOG_TAG,
                  "onServiceDisconnected() called with: " + "className = [" + className + "]");
            mRedditService = null;
        }
    }
}
