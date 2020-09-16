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
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.appyhigh.utilityapp.MainActivity
import com.appyhigh.utilityapp.R
import com.clevertap.android.sdk.CleverTapAPI
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * Created by tito on 27/12/17.
 */
class MyFirebaseMessaging : FirebaseMessagingService() {
    var bitmap: Bitmap? = null

    override fun onMessageSent(s: String) {
        super.onMessageSent(s)
        Log.d(TAG, "onMessageSent: $s")
    }

    override fun onSendError(s: String, e: Exception) {
        super.onSendError(s, e)
        Log.d(TAG, "onSendError: $e")
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Objects.requireNonNull(CleverTapAPI.getDefaultInstance(applicationContext))
            .pushFcmRegistrationId(s, true)
        Log.d(TAG, "onNewToken: $s")
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        try {
            // There are two types of messages data messages and notification messages. Data messages are handled
            // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
            // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
            // is in the foreground. When the app is in the background an automatically generated notification is displayed.
            // When the user taps on the notification they are returned to the app. Messages containing both notification
            // and data payloads are treated as notification messages. The Firebase console always sends notification
            // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
            Log.d(
                TAG,
                "From: " + remoteMessage.from
            )
            // Check if message contains a data payload.
            if (remoteMessage.data.isNotEmpty()) {
                Log.d(
                    TAG,
                    "Message data payload: " + remoteMessage.data
                )
            }
            // Check if message contains a notification payload.
            if (remoteMessage.notification != null) {
                Log.d(
                    TAG,
                    "Message Notification Body: " + remoteMessage.notification!!.body
                )
            }
            if (remoteMessage.data.isNotEmpty()) {
                val extras = Bundle()
                for ((key, value) in remoteMessage.data) {
                    extras.putString(key, value)
                }
                val info = CleverTapAPI.getNotificationInfo(extras)
                //message will contain the Push Message
                //imageUri will contain URL of the image to be displayed with Notification
                //link to send
                //P=Playstore, L=Link in app, B=external browser,D=Deeplink
                //To get a Bitmap image from the URL received
                var message: String?
                val imageUri: String?
                val link: String?
                val which: String?
                val title: String?
                if (info.fromCleverTap) {
                    if (extras.getString("nm") != "" || extras.getString("nm") != null
                    ) {
                        message = extras.getString("message")
                        message = extras.getString("nm")
                        imageUri = extras.getString("image")
                        link = extras.getString("link")
                        which = extras.getString("which")
                        title = extras.getString("title")
                        bitmap = getBitmapfromUrl(imageUri)
                        if (message != null) {
                            if (message != "") {
                                sendNotification(message, bitmap, which, link, title)
                            } else {
                                Objects.requireNonNull(
                                    CleverTapAPI.getDefaultInstance(
                                        applicationContext
                                    )
                                ).pushNotificationViewedEvent(extras)
                            }
                        }
                    }
                } else {
                    message = remoteMessage.data["message"]
                    imageUri = remoteMessage.data["image"]
                    link = remoteMessage.data["link"]
                    which = remoteMessage.data["which"]
                    title = remoteMessage.data["title"]
                    bitmap = getBitmapfromUrl(imageUri)
                    sendNotification(message, bitmap, which, link, title)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     */
    private fun sendNotification(
        messageBody: String?,
        image: Bitmap?,
        which: String?,
        link: String?,
        title: String?
    ) {
        try {
            Log.i("Result", "Got the data yessss")
            val rand = Random()
            val a = rand.nextInt(101) + 1
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("which", which)
            intent.putExtra("link", link)
            intent.putExtra("title", title)
            val pendingIntent = PendingIntent.getActivity(
                applicationContext, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT
            )
            val defaultSoundUri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(applicationContext)
                    .setLargeIcon(image) /*Notification icon image*/
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(messageBody)
                    .setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(image)
                    ) /*Notification with Image*/
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setPriority(Notification.PRIORITY_MAX)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // The id of the channel.
                val id = "messenger_general"
                val name: CharSequence = "General"
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
            e.printStackTrace()
        }
    }

    /*
     *To get a Bitmap image from the URL received
     * */
    private fun getBitmapfromUrl(imageUrl: String?): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection =
                url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val TAG = "FirebaseMessageService"
    }
}