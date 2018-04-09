package com.sjl.badger;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * App
 *
 * @author 林zero
 * @date 2018/4/9
 */

public class App extends Application implements Application.ActivityLifecycleCallbacks {
    public static int count = 0;
    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        if(AppUtil.isBackground(this)) {
            BadgerUtil.addBadger(this, count);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
