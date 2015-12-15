package test.fb.servicetest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fernandobarillas.redditservice.RedditService;
import com.fernandobarillas.redditservice.callbacks.RedditDataUpdatedCallback;
import com.fernandobarillas.redditservice.callbacks.RedditVoteCallback;
import com.fernandobarillas.redditservice.models.Link;
import com.fernandobarillas.redditservice.requests.SubredditRequest;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    private static final int UPDATE_INTERVAL = 1; // Seconds between UI updates from RedditService
    private RedditService mRedditService;
    private Handler elapsedTimeHandler;
    private TextView mElapsedTimeTextView;
    private Button mGetMoreLinksButton;
    private SubredditRequest mSubredditRequest;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mElapsedTimeTextView = (TextView) findViewById(R.id.elapsed_time);
        mGetMoreLinksButton = (Button) findViewById(R.id.get_links_button);

        setSupportActionBar(toolbar);
        mSubredditRequest = new SubredditRequest("all");

        mFab = (FloatingActionButton) findViewById(R.id.fab);

        elapsedTimeHandler = new Handler();
        Runnable updateTextView = new Runnable() {
            @Override
            public void run() {
                updateElapsedTimeTextView();
                elapsedTimeHandler.postDelayed(this, UPDATE_INTERVAL * 1000);
            }
        };

        elapsedTimeHandler.post(updateTextView);
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "onPause()");
        super.onPause();
        unbindService(mRedditServiceConnection);
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "onResume()");
        super.onResume();

        // Bind the reddit service to this Activity
        Intent redditServiceIntent = new Intent(this, RedditService.class);
        redditServiceIntent.putExtra(RedditService.REFRESH_TOKEN_KEY, PrivateConstants.REFRESH_TOKEN);
        redditServiceIntent.putExtra(RedditService.REDDIT_CLIENT_ID_KEY, PrivateConstants.REDDIT_CLIENT_ID);
        redditServiceIntent.putExtra(RedditService.REDDIT_REDIRECT_URL_KEY, PrivateConstants.REDDIT_REDIRECT_URL);
        bindService(redditServiceIntent, mRedditServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop()");
        stopService(new Intent(this, RedditService.class));
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onCreateOptionsMenu() called with: " + "menu = [" + menu + "]");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "onOptionsItemSelected() called with: " + "item = [" + item + "]");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateElapsedTimeTextView() {
        if (mElapsedTimeTextView == null || mRedditService == null) {
            return;
        }

        long elapsedTime = mRedditService.getRunningTime();
        int linkCount = mRedditService.getLinkCount();

        mElapsedTimeTextView.setText(
                String.format("%d second(s)\nLink Count: %d", elapsedTime, linkCount));
    }

    private RedditDataUpdatedCallback handleRedditDataUpdate(final View view) {
        return new RedditDataUpdatedCallback() {
            @Override
            public void dataUpdateCallback(Exception e) {
                Log.d(LOG_TAG, "dataUpdateCallback() called with: " + "e = [" + e + "]");
                if (view != null) {
                    Snackbar.make(view, "Downloaded more links", Snackbar.LENGTH_LONG)
                            .setAction("Action", null)
                            .show();
                }
                updateElapsedTimeTextView();
            }
        };
    }

    private ServiceConnection mRedditServiceConnection = new ServiceConnection() {
        private static final String LOG_TAG = "ServiceConnection";

        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            Log.d(LOG_TAG, "onServiceConnected() called with: " + "className = [" + className + "], iBinder = [" + iBinder + "]");
            RedditService.RedditBinder redditBinder = (RedditService.RedditBinder) iBinder;
            mRedditService = redditBinder.getService();
            if(mRedditService == null) {
                return;
                // TODO: Handle service unavailable, perform retries
            }
            mRedditService.getMoreLinks(mSubredditRequest, handleRedditDataUpdate(null));

            if (mFab != null) {
                mFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        final Link lastLink = mRedditService.getLink(mRedditService.getLinkCount() - 1);
                        if (lastLink != null) {
                            mRedditService.downvoteLink(lastLink, new RedditVoteCallback() {
                                @Override
                                public void voteCallback(Exception e) {
                                    if (e != null) {
                                        Snackbar.make(view, "Error when voting: " + e.getLocalizedMessage(),
                                                Snackbar.LENGTH_INDEFINITE).show();
                                    } else {
                                        Snackbar.make(view, "Downvoted " + lastLink.getTitle(),
                                                Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
            }
            if (mGetMoreLinksButton != null) {
                mGetMoreLinksButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        mRedditService.getMoreLinks(mSubredditRequest, handleRedditDataUpdate(view));
                    }
                });
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(LOG_TAG, "onServiceDisconnected() called with: " + "className = [" + className + "]");
            mRedditService = null;
        }
    };
}
