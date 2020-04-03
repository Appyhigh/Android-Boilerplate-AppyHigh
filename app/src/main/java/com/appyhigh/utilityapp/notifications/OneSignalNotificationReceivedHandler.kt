package com.appyhigh.utilityapp.notifications

import android.content.Context
import android.util.Log
import com.onesignal.OSNotification
import com.onesignal.OneSignal.NotificationReceivedHandler

class OneSignalNotificationReceivedHandler(private val context: Context) :
    NotificationReceivedHandler {
    override fun notificationReceived(notification: OSNotification) {
        val data = notification.payload.additionalData
        //While sending a Push notification from OneSignal dashboard
        // you can send an addtional data named "customkey" and retrieve the value of it and do necessary operation
        if (data != null) Log.d("OneSignalNotifRec", "data: $data")
    }
}