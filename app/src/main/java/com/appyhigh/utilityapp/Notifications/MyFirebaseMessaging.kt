package com.appyhigh.utilityapp.Notifications

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.appyhigh.utilityapp.R
import com.appyhigh.utilityapp.SplashActivity
import com.appyhigh.utilityapp.WebViewActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * Created by tito on 27/12/17.
 */
class MyFirebaseMessaging : FirebaseMessagingService() {
    var pattern = longArrayOf(500, 500, 500, 500, 500, 500, 500, 500, 500)
    var iUniqueId = (System.currentTimeMillis() and 0xfffffff).toInt()
    private var notificationManager: NotificationManager? = null
    private val ADMIN_CHANNEL_ID = "admin_channel"
    override fun onNewToken(fcmToken: String) {
        super.onNewToken(fcmToken)
        Log.e("Firebase Token", "****TOKEN***$fcmToken")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "MESSAGE payload: " + remoteMessage.data)
            val data: JSONObject
            if (remoteMessage.data.containsKey("which")) {
                try {
                    data = JSONObject(remoteMessage.data as Map<*, *>)
                    processingNotifications(data)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else if (remoteMessage.data.containsKey("title") && remoteMessage.data.containsKey("message")
            ) {
                try {
                    createNotification(remoteMessage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Throws(JSONException::class)
    private fun processingNotifications(data: JSONObject) {
        if (data.getString("which").equals("P", ignoreCase = true)) {
            goToApp(
                this,
                data.getString("title"),
                data.getString("message"),
                data.getString("image"),
                data.getString("playID")
            )
        } else if (data.getString("which").equals("B", ignoreCase = true)) {
            goToWebPage(
                this,
                data.getString("title"),
                data.getString("message"),
                data.getString("image"),
                data.getString("url")
            )
        } else if (data.getString("which").equals("L", ignoreCase = true)) {
            goToWebView(
                this,
                data.getString("title"),
                data.getString("message"),
                data.getString("image"),
                data.getString("url")
            )
        } else if (data.getString("which").equals("in-app-screens", ignoreCase = true)) {
            goToActivity(
                this,
                data.getString("title"),
                data.getString("message"),
                data.getString("image"),
                data.getString("page")
            )
        } else if (data.getString("which").equals("app-home-screens", ignoreCase = true)) {
            goToHomePage(
                this,
                data.getString("title"),
                data.getString("message"),
                data.getString("image"),
                data.getString("screen"),
                data
            )
        }
    }

    @Throws(JSONException::class)
    private fun createNotification(remoteMessage: RemoteMessage) {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val data = JSONObject(remoteMessage.data as Map<*, *>)
        //Setting up Notification channels for android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels()
        }
        val notificationId = Random().nextInt(60000)
        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder =
            NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo) //a resource for your custom small icon
                .setContentTitle(data.getString("title")) //the "title" value you sent in your notification
                .setContentText(data.getString("message")) //message
                .setAutoCancel(true) //dismisses the notification on click
                .setSound(defaultSoundUri)
        notificationManager!!.notify(notificationId, notificationBuilder.build())
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels() {
        val adminChannelName: CharSequence =
            getString(R.string.notifications_admin_channel_name)
        val adminChannelDescription =
            getString(R.string.notifications_admin_channel_description)
        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(
            ADMIN_CHANNEL_ID,
            adminChannelName,
            NotificationManager.IMPORTANCE_LOW
        )
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        if (notificationManager != null) {
            notificationManager!!.createNotificationChannel(adminChannel)
        }
    }

    private fun goToWebView(
        context: Context,
        title: String,
        message: String,
        image: String,
        url: String
    ) {
        try {
            val notificationIntent = Intent(this, WebViewActivity::class.java)
            notificationIntent.putExtra("url", url)
            notificationIntent.putExtra("title", title)
            notificationIntent.flags =
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
            val contentIntent = PendingIntent.getActivity(
                context,
                (Math.random() * 100).toInt(),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notificationBuilder = NotificationCompat.Builder(context, "deeplink")
                .setContentTitle(title)
                .setContentText(message)
                .setLights(Color.BLUE, 500, 500)
                .setVibrate(pattern)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setLargeIcon(getBitmapfromUrl(image))
                .setSmallIcon(R.drawable.logo)
                .setStyle(NotificationCompat.BigPictureStyle().bigPicture(getBitmapfromUrl(image)).bigLargeIcon(null)).setAutoCancel(true)
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationBuilder.setContentIntent(contentIntent)
            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "deeplink",
                    "Deep-linking",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager!!.createNotificationChannel(channel)
            }
            notificationManager!!.notify(iUniqueId, notificationBuilder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun goToHomePage(
        context: Context?,
        title: String?,
        message: String?,
        image: String?,
        pageNo: String?,
        data: JSONObject
    ) {
        try {
            val notificationIntent = Intent(context, SplashActivity::class.java)
            notificationIntent.putExtra("notification_data", data.toString())
            notificationIntent.putExtra("screen", pageNo)
            notificationIntent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP
                    or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val contentIntent = PendingIntent.getActivity(
                context,
                (Math.random() * 100).toInt(),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val drivingNotifBldr =
                NotificationCompat.Builder(context!!, "deeplink")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setLights(Color.BLUE, 500, 500)
                    .setVibrate(pattern)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setLargeIcon(getBitmapfromUrl(image))
                    .setSmallIcon(R.drawable.logo)
                    .setAutoCancel(true)
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            drivingNotifBldr.setContentIntent(contentIntent)
            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "deeplink",
                    "Deep-linking",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager!!.createNotificationChannel(channel)
            }
            notificationManager!!.notify(iUniqueId, drivingNotifBldr.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun goToActivity(
        context: Context?,
        title: String?,
        message: String?,
        image: String?,
        activityName: String
    ) {
        try {
            var notificationIntent: Intent? = null
            try {
                notificationIntent = Intent(
                    context,
                    Class.forName("com.appyhigh.utilityapp$activityName")
                )
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
            notificationIntent!!.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP
                    or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val contentIntent = PendingIntent.getActivity(
                context,
                (Math.random() * 100).toInt(),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val drivingNotifBldr =
                NotificationCompat.Builder(context!!, "deeplink")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setLights(Color.BLUE, 500, 500)
                    .setVibrate(pattern)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setLargeIcon(getBitmapfromUrl(image))
                    .setSmallIcon(R.drawable.logo)
                    .setAutoCancel(true)
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            drivingNotifBldr.setContentIntent(contentIntent)
            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "deeplink",
                    "Deep-linking",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager!!.createNotificationChannel(channel)
            }
            notificationManager!!.notify(iUniqueId, drivingNotifBldr.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun goToWebPage(
        context: Context?,
        title: String?,
        message: String?,
        image: String?,
        url: String?
    ) {
        try {
            val notificationIntent = Intent(Intent.ACTION_VIEW)
            notificationIntent.data = Uri.parse(url)
            notificationIntent.flags =
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
            val contentIntent = PendingIntent.getActivity(
                context,
                (Math.random() * 100).toInt(),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val drivingNotifBldr =
                NotificationCompat.Builder(context!!, "deeplink")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setLights(Color.BLUE, 500, 500)
                    .setVibrate(pattern)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setLargeIcon(getBitmapfromUrl(image))
                    .setSmallIcon(R.drawable.logo)
                    .setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(getBitmapfromUrl(image))
                            .bigLargeIcon(null)
                    )
                    .setAutoCancel(true)
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            drivingNotifBldr.setContentIntent(contentIntent)
            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "deeplink",
                    "Deep-linking",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager!!.createNotificationChannel(channel)
            }
            notificationManager!!.notify(iUniqueId, drivingNotifBldr.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun goToApp(
        context: Context?,
        title: String?,
        message: String?,
        image: String?,
        playID: String
    ) {
        try {
            val uri = Uri.parse("market://details?id=$playID")
            val notificationIntent = Intent(Intent.ACTION_VIEW, uri)
            notificationIntent.flags =
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
            val taskStackBuilder =
                TaskStackBuilder.create(context!!)
            taskStackBuilder.addNextIntentWithParentStack(notificationIntent)
            val contentIntent = PendingIntent.getActivity(
                context,
                (Math.random() * 100).toInt(),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val drivingNotifBldr =
                NotificationCompat.Builder(context, "deeplink")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setLights(Color.BLUE, 500, 500)
                    .setVibrate(pattern)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setLargeIcon(getBitmapfromUrl(image))
                    .setSmallIcon(R.drawable.logo)
                    .setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(getBitmapfromUrl(image))
                            .bigLargeIcon(null)
                    )
                    .setAutoCancel(true)
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            drivingNotifBldr.setContentIntent(contentIntent)
            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "deeplink",
                    "Deep-linking",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager!!.createNotificationChannel(channel)
            }
            notificationManager!!.notify(iUniqueId, drivingNotifBldr.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBitmapfromUrl(imageUrl: String?): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection =
                url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(input)
            getRoundedCornerBitmap(
                applicationContext,
                bitmap,
                20,
                bitmap.width,
                bitmap.height,
                false,
                false,
                false,
                false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getRoundedCornerBitmap(
        context: Context,
        input: Bitmap?,
        pixels: Int,
        w: Int,
        h: Int,
        squareTL: Boolean,
        squareTR: Boolean,
        squareBL: Boolean,
        squareBR: Boolean
    ): Bitmap {
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val densityMultiplier = context.resources.displayMetrics.density
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, w, h)
        val rectF = RectF(rect)
        //make sure that our rounded corner is scaled appropriately
        val roundPx = pixels * densityMultiplier
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
        //draw rectangles over the corners we want to be square
        if (squareTL) {
            canvas.drawRect(0f, 0f, w / 2.toFloat(), h / 2.toFloat(), paint)
        }
        if (squareTR) {
            canvas.drawRect(w / 2.toFloat(), 0f, w.toFloat(), h / 2.toFloat(), paint)
        }
        if (squareBL) {
            canvas.drawRect(0f, h / 2.toFloat(), w / 2.toFloat(), h.toFloat(), paint)
        }
        if (squareBR) {
            canvas.drawRect(w / 2.toFloat(), h / 2.toFloat(), w.toFloat(), h.toFloat(), paint)
        }
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(input!!, 0f, 0f, paint)
        return output
    }

    fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val runningProcesses =
                Objects.requireNonNull(am).runningAppProcesses
            for (processInfo in runningProcesses) {
                if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.packageName) {
                            isInBackground = false
                        }
                    }
                }
            }
        } else {
            val taskInfo =
                Objects.requireNonNull(am).getRunningTasks(1)
            val componentInfo = taskInfo[0].topActivity
            if (componentInfo != null && componentInfo.packageName == context.packageName) {
                isInBackground = false
            }
        }
        return isInBackground
    }

    //method of running
    val isActivityRunning: Boolean
        get() {
            val activityManager =
                this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val activitys =
                activityManager.getRunningTasks(Int.MAX_VALUE)
            var isActivityFound = false
            for (i in activitys.indices) {
                if (activitys[i].topActivity.toString()
                        .equals("com.appyhigh.utilityapp.MainActivity", ignoreCase = true)
                ) {
                    isActivityFound = true
                }
            }
            return isActivityFound
        }

    companion object {
        private const val TAG = "MyFirebaseMessaging"
    }
}