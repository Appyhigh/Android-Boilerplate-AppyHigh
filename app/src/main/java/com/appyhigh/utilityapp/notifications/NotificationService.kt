package com.appyhigh.utilityapp.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.appyhigh.utilityapp.MainActivity
import com.appyhigh.utilityapp.R
import com.crashlytics.android.Crashlytics
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class NotificationService : FirebaseMessagingService() {
    override fun onNewToken(fcmToken: String) {
        super.onNewToken(fcmToken)
        Log.e("Firebase Token", "****TOKEN***$fcmToken")
    }

    val TAG = "NotificationService"
    lateinit var bitmap: Bitmap
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        try {
            if (remoteMessage.data.isNotEmpty()) {
                Log.d(TAG, "Message data payload: " + remoteMessage.data)
            }

            if (remoteMessage.notification != null) {
                Log.d(TAG, "Message Notification Body: " + remoteMessage.notification!!.body!!)
            }
            var title = remoteMessage.data["title"]
            var message = remoteMessage.data["message"]
            var subtext = remoteMessage.data["subtext"]
            var imageUri = remoteMessage.data["image"]
            var link = remoteMessage.data["link"]
            //P=Playstore, L=Link in app, B=external browser
            var which = remoteMessage.data["which"]


            Log.e(TAG, "onMessageReceived: which" + which!!)
            Log.e(TAG, "onMessageReceived: link" + link!!)
            Log.e(TAG, "onMessageReceived: imageUri" + imageUri!!)
            Log.e(TAG, "onMessageReceived: message" + message!!)
            Log.e(TAG, "onMessageReceived: subtext" + subtext!!)
            Log.e(TAG, "onMessageReceived: title" + title!!)

            //To get a Bitmap image from the URL received
            bitmap = getBitmapfromUrl(imageUri)!!

            if (message == null || message.isEmpty()) {
                return
            }
            sendNotification(title, message, subtext, bitmap, which, link)
        } catch (e: Exception) {

        }

    }

    private fun sendNotification(
        title: String,
        message: String,
        subtext: String,
        image: Bitmap,
        which: String,
        link: String
    ) {
        try {
            Log.i("Result", "Got the data yessss")


            val rand = Random()
            val a = rand.nextInt(101) + 1

            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("which", which)
            intent.putExtra("link", link)
            intent.putExtra("title", title)
            val pendingIntent = PendingIntent.getActivity(
                this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT
            )

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)


            val notificationBuilder = NotificationCompat.Builder(this)
                .setLargeIcon(image)/*Notification icon image*/
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setSubText(subtext)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(image)
                )/*Notification with Image*/
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_MAX)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // The id of the channel.
                val id = "messenger_general"

                val name = "General"

                val description = "General Notifications sent by the app"

                val importance = NotificationManager.IMPORTANCE_HIGH

                val mChannel = NotificationChannel(id, name, importance)

                mChannel.description = description

                mChannel.enableLights(true)
                mChannel.lightColor = Color.BLUE

                mChannel.enableVibration(true)

                notificationManager.createNotificationChannel(mChannel)
                notificationManager.notify(a + 1, notificationBuilder.setChannelId(id).build())
            } else {
                notificationManager.notify(
                    a + 1 /* ID of notification */,
                    notificationBuilder.build()
                )
            }


        } catch (e: Exception) {
            try {
                Crashlytics.logException(e)
            } catch (e2: Exception) {
            }

        }

    }

    /*
    *To get a Bitmap image from the URL received
    * */
    fun getBitmapfromUrl(imageUrl: String): Bitmap? {
        try {
            Log.e(TAG, "getBitmapfromUrl: $imageUrl")
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            return BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.e(TAG, "getBitmapfromUrl: " + e.message)
            e.printStackTrace()
            return null

        }

    }
}