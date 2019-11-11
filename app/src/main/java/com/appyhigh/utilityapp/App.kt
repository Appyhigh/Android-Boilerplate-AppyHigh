package com.appyhigh.utilityapp

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.facebook.FacebookSdk
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.onesignal.OSNotificationOpenResult
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
        OneSignal.startInit(this)
            .setNotificationOpenedHandler(OneSignalNotifOpenHandler())
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init()
    }

    inner class OneSignalNotifOpenHandler : OneSignal.NotificationOpenedHandler{
        override fun notificationOpened(notification: OSNotificationOpenResult?) {
            try {
                var which = notification!!.notification.payload.additionalData.getString("which")
                var link = notification!!.notification.payload.additionalData.getString("link")
                var title = notification!!.notification.payload.title

                when(which){
                    "P"->{
                        try {
                            val i =
                                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$link"))
                            i.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(i)
                        } catch (anfe: android.content.ActivityNotFoundException) {
                            val i = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=$link")
                            )
                            i.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(i)
                        }

                    }

                    "B"->{
                        try {
                            val i = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                            i.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(i)
                        } catch (anfe: android.content.ActivityNotFoundException) {

                        }
                    }

                    "L"->{
                        var i = Intent(applicationContext, WebViewActivity::class.java)
                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        i.putExtra("url", link)
                        i.putExtra("title", title)
                        startActivity(i)
                    }
                }
            }catch ( e : Exception){
                Log.e(TAG, "checkForNotifications: $e")
            }
        }
    }

}