package com.appyhigh.utilityapp

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.appyhigh.utilityapp.events.AnalyticsManager
import com.appyhigh.utilityapp.notifications.OneSignalNotifOpenHandler
import com.appyhigh.utilityapp.notifications.OneSignalNotificationReceivedHandler
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.AudienceNetworkAds.InitListener
import com.facebook.ads.AudienceNetworkAds.InitResult
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.messaging.FirebaseMessaging
import com.onesignal.OneSignal


class App : Application() {
    val TAG = "App"

    companion object {
        lateinit var firebaseAnalytics: FirebaseAnalytics
    }

    override fun onCreate() {
        super.onCreate()
        AudienceNetworkAds.initialize(applicationContext)
        AudienceNetworkInitializeHelper.initialize(applicationContext)
        FirebaseMessaging.getInstance().subscribeToTopic("ALLUSERS")
        FirebaseMessaging.getInstance().subscribeToTopic("UtilityApp")
        if (BuildConfig.DEBUG) {
            FirebaseMessaging.getInstance().subscribeToTopic("UtilityAppDebug")
        }
        /*FirebaseAnalytics*/
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        /*mobile ads*/
        MobileAds.initialize(this, OnInitializationCompleteListener { })

        // OneSignal Initialization
        OneSignal.startInit(this)
            .setNotificationOpenedHandler(OneSignalNotifOpenHandler(applicationContext))
            .setNotificationReceivedHandler(OneSignalNotificationReceivedHandler(applicationContext))
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init()
        /*FirebaseInAppMessaging*/
        FirebaseInAppMessaging.getInstance().setMessagesSuppressed(true)

        /*Events*/
        AnalyticsManager.initialize(this)
        /*single event*/
        AnalyticsManager.logEvent("AppOpened")
        /*event with params*/
        val bundle = Bundle()
        with(bundle) {
            putString(
                "Time",
                System.currentTimeMillis().toString()
            )
        }
        AnalyticsManager.logEvent("AppOpened", bundle)
    }

    class AudienceNetworkInitializeHelper : InitListener {
        override fun onInitialized(result: InitResult) {
            Log.d(AudienceNetworkAds.TAG, result.message)
        }

        companion object {
            /**
             * It's recommended to call this method from Application.onCreate().
             * Otherwise you can call it from all Activity.onCreate()
             * methods for Activities that contain ads.
             *
             * @param context Application or Activity.
             */
            fun initialize(context: Context?) {
                if (!AudienceNetworkAds.isInitialized(context)) {
                    if (BuildConfig.DEBUG) {
                        AdSettings.turnOnSDKDebugger(context)
                    }
                    AudienceNetworkAds
                        .buildInitSettings(context)
                        .withInitListener(AudienceNetworkInitializeHelper())
                        .initialize()
                }
            }
        }
    }
}