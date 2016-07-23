package test.fb.servicetest;

import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;

import timber.log.Timber;

/**
 * Created by fb on 7/20/16.
 */
public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected String createStackElementTag(StackTraceElement element) {
                    return "(" + element.getFileName() + ":" + element.getLineNumber() + ")";
                }
            });
        }
        Timber.v("onCreate() called");
    }

    @Override
    public void onTerminate() {
        Timber.v("onTerminate() called");
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Timber.v("onConfigurationChanged() called with: " + "newConfig = [" + newConfig + "]");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        Timber.v("onLowMemory() called");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        Timber.v("onTrimMemory() called with: " + "level = [" + level + "]");
        super.onTrimMemory(level);
    }

    @Override
    public void registerComponentCallbacks(ComponentCallbacks callback) {
        Timber.v("registerComponentCallbacks() called with: " + "callback = [" + callback + "]");
        super.registerComponentCallbacks(callback);
    }

    @Override
    public void unregisterComponentCallbacks(ComponentCallbacks callback) {
        Timber.v("unregisterComponentCallbacks() called with: " + "callback = [" + callback + "]");
        super.unregisterComponentCallbacks(callback);
    }

    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        Timber.v("registerActivityLifecycleCallbacks() called with: "
                + "callback = ["
                + callback
                + "]");
        super.registerActivityLifecycleCallbacks(callback);
    }

    @Override
    public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        Timber.v("unregisterActivityLifecycleCallbacks() called with: "
                + "callback = ["
                + callback
                + "]");
        super.unregisterActivityLifecycleCallbacks(callback);
    }

    @Override
    public void registerOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        Timber.v("registerOnProvideAssistDataListener() called with: "
                + "callback = ["
                + callback
                + "]");
        super.registerOnProvideAssistDataListener(callback);
    }

    @Override
    public void unregisterOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        Timber.v("unregisterOnProvideAssistDataListener() called with: "
                + "callback = ["
                + callback
                + "]");
        super.unregisterOnProvideAssistDataListener(callback);
    }
}
