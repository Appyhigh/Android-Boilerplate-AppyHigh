package com.appyhigh.utilityapp.Notifications

import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.appyhigh.utilityapp.SplashActivity
import com.appyhigh.utilityapp.WebViewActivity
import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal.NotificationOpenedHandler
import org.json.JSONException

class OneSignalNotifOpenHandler(private val context: Context) :
    NotificationOpenedHandler {
    private fun isAppRunning(
        context: Context,
        packageName: String
    ): Boolean {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val procInfos =
            activityManager.runningAppProcesses
        if (procInfos != null) {
            for (processInfo in procInfos) {
                if (processInfo.processName == packageName) {
                    return true
                }
            }
        }
        return false
    }

    // This fires when a notification is opened by tapping on it.
    override fun notificationOpened(result: OSNotificationOpenResult) {
        val actionType = result.action.type
        val data = result.notification.payload.additionalData
        //While sending a Push notification from OneSignal dashboard
        //Else, if we have not set any additional data MainActivity is opened.
        if (data != null) {
            try {
                var which = ""
                var url: String? = ""
                var title: String? = ""
                var message: String? = ""
                var image: String? = ""
                var playId = ""
                try {
                    title = result.notification.payload.additionalData.getString("title")
                    message = result.notification.payload.additionalData.getString("message")
                    which = result.notification.payload.additionalData.getString("which")
                    image = result.notification.payload.additionalData.getString("image")
                    if (which == "P") {
                        playId = result.notification.payload.additionalData.getString("playID")
                    } else {
                        url = result.notification.payload.additionalData.getString("url")
                    }
                    if (which == "") {
                        if (data.getString("title") != "" && data.getString("message") != "") {
                            if (!isAppRunning(context, context.packageName)) {
                                val i = Intent(context, SplashActivity::class.java)
                                context.startActivity(i)
                            }
                        }
                    } else {
                        when (which) {
                            "P" -> try {
                                val i = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=$playId")
                                )
                                i.flags =
                                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(i)
                            } catch (e: ActivityNotFoundException) {
                                val i = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=$playId")
                                )
                                i.flags =
                                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(i)
                            }
                            "B" -> try {
                                val i = Intent(Intent.ACTION_VIEW)
                                i.data = Uri.parse(url)
                                i.flags =
                                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(i)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            "L" -> try {
                                val i = Intent(context, WebViewActivity::class.java)
                                i.putExtra("url", url)
                                i.putExtra("title", title)
                                i.flags =
                                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(i)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}