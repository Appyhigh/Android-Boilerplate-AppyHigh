package com.appyhigh.utilityapp

import android.app.Application
import com.appyhigh.utilityapp.notifications.OneSignalNotifOpenHandler
import com.appyhigh.utilityapp.notifications.OneSignalNotificationReceivedHandler
import com.crashlytics.android.Crashlytics
import com.facebook.FacebookSdk
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.onesignal.OneSignal
import io.fabric.sdk.android.Fabric

class App : Application() {
    val TAG = "App"

    companion object {
        lateinit var firebaseAnalytics: FirebaseAnalytics
    }

    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(applicationContext)
        AudienceNetworkAds.initialize(this)
        AudienceNetworkAds.isInAdsProcess(this)
        FirebaseMessaging.getInstance().subscribeToTopic("ALLUSERS")
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        MobileAds.initialize(this, getString(R.string.admob_app_id))
        Fabric.with(this, Crashlytics())
        // OneSignal Initialization
        OneSignal.startInit(this)
            .setNotificationOpenedHandler(OneSignalNotifOpenHandler(applicationContext))
            .setNotificationReceivedHandler(OneSignalNotificationReceivedHandler(applicationContext))
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init()
    }
}