package com.wedo8020.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.wedo8020.app.utils.Prefs

class AppLifecycleObserver : Application.ActivityLifecycleCallbacks {

    var activity: Activity? = null
    private var foregroundActivities = 0

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // No implementation needed
    }

    override fun onActivityStarted(activity: Activity) {
        foregroundActivities++

        // Your app is in the foreground
        if (foregroundActivities == 1) {
            Prefs.init().isAppBackground = false
        }
    }

    override fun onActivityResumed(activity: Activity) {
        // No implementation needed
        this.activity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity == this.activity) {
            this.activity = null
        }
    }

    override fun onActivityStopped(activity: Activity) {
        foregroundActivities--

        // Your app is in the background
        if (foregroundActivities == 0) {
            Prefs.init().isAppBackground = true
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // No implementation needed
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity == this.activity) {
            this.activity = null
        }
    }
}
