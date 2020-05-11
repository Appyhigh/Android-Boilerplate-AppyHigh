# Android Boilerplate AppyHigh
## Firebase In-App Messaging
1. Make sure you have added firebase to your project.  
2. Add the following lines to your app level build.gradle file.
```
implementation 'com.google.firebase:firebase-inappmessaging-display:19.0.6'
implementation 'com.google.firebase:firebase-analytics:17.4.0'
```
3. To handle message clicks, 
	* Add the following import statements in your Kotlin code.
	```
	import com.google.firebase.inappmessaging.FirebaseInAppMessagingClickListener
	import com.google.firebase.inappmessaging.model.Action
	import com.google.firebase.inappmessaging.model.InAppMessage
	```
	* Extend *FirebaseInAppMessagingClickListener* and override *messageClicked* function as shown
	```
	override fun messageClicked(
        inAppMessage: InAppMessage,
        action: Action
    ) {
        Log.d(
            TAG,
            "messageClicked() called with: inAppMessage = [$inAppMessage], action = [$action]"
        )
        // Determine which URL the user clicked
        val url = action.actionUrl
        Log.d(TAG, "messageClicked: $url")
        // Get general information about the campaign
        val metadata = inAppMessage.campaignMetadata
        // Get data bundle for the inapp message
        val dataBundle: Map<*, *>? = inAppMessage.data
    }

    companion object {
        private const val TAG = "MyClickListener"
    }
	```
  
## Push Notifications
### OneSignal
Follow the steps at [OneSignal Android SDK Setup](https://documentation.onesignal.com/docs/android-sdk-setup) to setup the OneSignal SDK.  
1. Add the following import statements to the top of your file  
```
import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal.NotificationOpenedHandler
```
2. Extend *NotificationOpenedHandler* and override *notificationOpened* function as shown
```
class OneSignalNotifOpenHandler(private val context: Context) :
    NotificationOpenedHandler {

    // This fires when a notification is opened by tapping on it.
    override fun notificationOpened(result: OSNotificationOpenResult) {
        val actionType = result.action.type
        val data = result.notification.payload.additionalData
           
    }
}
```
3. Retrieve the data received in the notification
```
var which = ""
var url: String? = ""
var title: String? = ""
var message: String? = ""
var image: String? = ""
var playId = ""
title = result.notification.payload.additionalData.getString("title")
message = result.notification.payload.additionalData.getString("message")
which = result.notification.payload.additionalData.getString("which")
image = result.notification.payload.additionalData.getString("image")
url = result.notification.payload.additionalData.getString("url")
```
4. We have 4 types of push notifications **P**, **B**, **L** and **D** which are identified by the *which* parameter.
	* **P** type notifications must open the play store.
	```
	try {
	    val i = Intent(
	            Intent.ACTION_VIEW,
	            Uri.parse("market://details?id=$url")
	    )
	    i.flags =
	        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
	    context.startActivity(i)
	} catch (e: ActivityNotFoundException) {
	        val i = Intent(
	                Intent.ACTION_VIEW,
	                Uri.parse("https://play.google.com/store/apps/details?id=$url")
	        )
	        i.flags =
	                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
	        context.startActivity(i)
	}
	```  
	* Expected JSON for **P** type:  
	```
	{
		"to": "/topics/AppNameDebug/AppName/ALLUSERS",
		"data": {
			"title": "No lockdown in this country, may get worse than Italy",
			"message": "No lockdown in this country, may get worse than Italy",
			"image": "https://lh3.googleusercontent.com/Yj311yVdDGCzDwrN24BFZaaX2X2c2f4G9R6sIcf6aU9ApmsNibo-zgO7MjrDj7TdfHQ=s180-rw",
			"url": "darkmode.theme.ig.sms.fb.android",
			"which": "P"
		}
	}
	```
	* **B** type notifications must open the default browser.
	```
	val i = Intent(Intent.ACTION_VIEW)
	i.data = Uri.parse(url)
	i.flags =
	        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
	context.startActivity(i)
	```
	* Expected JSON for **B** type:  
	```
	{
		"to": "/topics/AppNameDebug/AppName/ALLUSERS",
		"data": {
			"title": "No lockdown in this country, may get worse than Italy",
			"message": "No lockdown in this country, may get worse than Italy",
			"image": "https://i.imgur.com/h9IA6TJ.jpg",
			"url": "https://asviral.com/",
			"which": "B"
		}
	}
	```	 
	* **L** type notifications must open the webview within the app.
	```
	val i = Intent(context, WebViewActivity::class.java)
	i.putExtra("url", url)
	i.putExtra("title", title)
	i.putExtra("option_flag", true)
	i.flags =
	        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
	context.startActivity(i)
	```
	* Expected JSON for **L** type:  
	```
	{
		"to": "/topics/AppNameDebug/AppName/ALLUSERS",
		"data": {
			"title": "No lockdown in this country, may get worse than Italy",
			"message": "No lockdown in this country, may get worse than Italy",
			"image": "https://i.imgur.com/h9IA6TJ.jpg",
			"url": "https://asviral.com/",
			"which": "L"
		}
	}
	```	
	* **D** type notification must open a specific page within the app.
	```
	val i = Intent(context, MainActivity::class.java)
	i.putExtra("which", which)
	i.putExtra("url", url)
	i.putExtra("title", title)
	i.putExtra("option_flag", true)
	i.flags =
	        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
	context.startActivity(i)
	```
	* Expected JSON for **D** type:  
	```
	{
		"to": "/topics/AppNameDebug/AppName/ALLUSERS",
		"data": {
			"title": "No lockdown in this country, may get worse than Italy",
			"message": "No lockdown in this country, may get worse than Italy",
			"image": "https://i.imgur.com/h9IA6TJ.jpg",
			"url": "AppName://ACTIVITYNAME",
			"which": "D"
		}
	}
	```	

### Firebase Cloud Messaging
Follow the steps at [Firebase Cloud Messaging Android Setup](https://firebase.google.com/docs/cloud-messaging/android/client) to setup the Firebase Cloud Messaging SDK.  
1. Add the following import statements to the top of your file  
```
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
```
2. Extend *FirebaseMessagingService* and override *onMessageReceived* function as shown
```
class MyFirebaseMessaging : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data.isNotEmpty()) {
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
}
```
3. If the recieved notification has a *which* parameter, then it is one of the type **P**, **B**, **L** or **D**.
```
@Throws(JSONException::class)
    private fun processingNotifications(data: JSONObject) {
        when {
            data.getString("which").equals("P", ignoreCase = true) -> {
                goToPlaystore(this,data.getString("title"),data.getString("message"),data.getString("image",data.getString("playID")
                )
            }
            data.getString("which").equals("B", ignoreCase = true) -> {
                goToWebPage(this,data.getString("title"),data.getString("message"),data.getString("image"),data.getString("url")
                )
            }
            data.getString("which").equals("L", ignoreCase = true) -> {
                goToWebView(this,data.getString("title"),data.getString("message"),data.getString("image"),data.getString("url")
                )
            }
            data.getString("which").equals("D", ignoreCase = true) -> {
                goToActivity(this,data.getString("title"),data.getString("message"),data.getString("image"),data.getString("url"),data.getString("which")
                )
            }
        }
    }
```
4. On receiving the message from firebase, we must create notifications. For the functions to create notifications, and the above mentioned functions, visit [this page](https://github.com/aneeshrayan/Android-Boilerplate-AppyHigh/blob/master/app/src/main/java/com/appyhigh/utilityapp/notifications/MyFirebaseMessaging.kt)

## Ads
### AdMob  
1. Add Google's maven repository to your project-level build.gradle file  
```
allprojects {
    repositories {
        google()
    }
}
```  
2. Add the following line to dependencies in your app-level build.gradle file
```
implementation 'com.google.android.gms:play-services-ads:19.1.0'
```   
3. Add the following lines inside the *application* tag in *AndroidManifest.xml* file
```
<meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-3940256099942544~3347511713"/>
```
4. Replace the sample AdMob App ID with the real App Id before publishing.  
5. Add the following import statement to your activity
```
import com.google.android.gms.ads.MobileAds;
```
6. Add the following line inside the *onCreate* method to initialize mobile ads
```
MobileAds.initialize(this, OnInitializationCompleteListener {})
```
  
### Facebook Ads
1. To register your app for Facebook Ads click [here](https://developers.facebook.com/docs/app-ads/app-setup).  
2. Add the following lines to the dependencies section of app-level *build.gradle* file
```
implementation 'com.android.support:support-annotations:28.0.0'
implementation 'com.facebook.android:audience-network-sdk:5.8.0'
implementation 'com.google.ads.mediation:facebook:5.6.1.0'
```
3. Add the following lines inside *application tag* in *AndroidManifest.xml* file, and replace *facebook_app_id* with the real App ID.
```
<meta-data
            android:name="com.facebook.sdk.ApplicationId"
            android:value="facebook_app_id" />
	    
<activity android:name="com.facebook.ads.AudienceNetworkActivity"
          android:configChanges="keyboardHidden|orientation|screenSize"/>
```  
4. To initialize Facebook Ads, use the following import statement and code
```
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
//Inside onCreate()
AudienceNetworkAds.initialize(applicationContext);
AudienceNetworkInitializeHelper.initialize(applicationContext)
```
5. To learn how to implement various kinds of Ads, click [here](https://github.com/aneeshrayan/Android-Boilerplate-AppyHigh/blob/master/app/src/main/java/com/appyhigh/utilityapp/MainActivity.kt).  
  
## Firebase Remote Config  
1. Add the following lines to the app-level *build.gradle* file  
```
implementation 'com.google.firebase:firebase-config-ktx:19.1.4'
implementation 'com.google.firebase:firebase-analytics:17.4.0'
```  
2. Get a Remote Config instance and set a minimum interval for refreshes
```
remoteConfig = Firebase.remoteConfig
val configSettings = remoteConfigSettings {
    minimumFetchIntervalInSeconds = 3600
}
remoteConfig.setConfigSettingsAsync(configSettings)
```
3. Create a *remote_config_defaults.xml* file in *res/xml* folder, and set default key value pairs as shown
```
<defaultsMap>
    <entry>
        <key>KEY</key>
        <value>VALUE</value>
    </entry>
</defaultsMap>
```  
4. To fetch and replace the local values with values from Remote Config backend, use the following function
```
remoteConfig.fetchAndActivate()
        .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val updated = task.result
                Log.d(TAG, "Config params updated: $updated")
                Toast.makeText(this, "Fetch and activate succeeded",
                        Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Fetch failed",
                        Toast.LENGTH_SHORT).show()
            }
            displayWelcomeMessage()
        }
```
  
## Dynamic Linking
1. Add the following lines to the app-level *build.gradle* file  
```
implementation 'com.google.firebase:firebase-dynamic-links-ktx:19.1.0'
implementation 'com.google.firebase:firebase-analytics:17.4.0'
```  
2. To create long dynamic links that opens the link on you app, use the following code
```
val dynamicLink = Firebase.dynamicLinks.dynamicLink {
    link = Uri.parse("https://www.example.com/")
    domainUriPrefix = "https://example.page.link"
    // Open links with this app on Android
    androidParameters { }
    // Open links with com.example.ios on iOS
    iosParameters("com.example.ios") { }
}

val dynamicLinkUri = dynamicLink.uri
```
3. To receive Dynamic Links, add the following intent-filter to your *AndroidManifest.xml* file
```
<intent-filter>
    <action android:name="android.intent.action.VIEW"/>
    <category android:name="android.intent.category.DEFAULT"/>
    <category android:name="android.intent.category.BROWSABLE"/>
    <data
        android:host="example.com"
        android:scheme="https"/>
</intent-filter>
```
4. To receive the deep link, call the *getDynamicLink()* method as shown
```
Firebase.dynamicLinks
        .getDynamicLink(intent)
        .addOnSuccessListener(this) { pendingDynamicLinkData ->
            // Get deep link from result (may be null if no link is found)
            var deepLink: Uri? = null
            if (pendingDynamicLinkData != null) {
                deepLink = pendingDynamicLinkData.link
            }

            // Handle the deep link. For example, open the linked content.
        }
        .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }
```
  
## Checking for in-app updates
To check for updates, call the below function. This task is usually done before the user reaches the home screen
```
    private fun checkForLatestVersion() {
        if (BuildConfig.DEBUG) {
            nextActivity()
        } else {// Creates instance of the manager.
            val appUpdateManager = AppUpdateManagerFactory.create(this@SplashActivity)
            appUpdateManager
                .appUpdateInfo
                .addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        // If an in-app update is already running, resume the update.
                        try {
                            appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.IMMEDIATE,
                                this,
                                212
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            e.printStackTrace()
                            nextActivity()
                        }

                    } else {
                        // Returns an intent object that you use to check for an update.
                        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

                        // Checks that the platform will allow the specified type of update.
                        appUpdateInfoTask.addOnSuccessListener { appUpdateInfoNew ->
                            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                                // For a flexible update, use AppUpdateType.FLEXIBLE
                                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                            ) {
                                // Request the update.
                                try {
                                    appUpdateManager.startUpdateFlowForResult(
                                        appUpdateInfoNew,
                                        AppUpdateType.IMMEDIATE,
                                        this@SplashActivity,
                                        212
                                    )
                                } catch (e: IntentSender.SendIntentException) {
                                    e.printStackTrace()
                                    nextActivity()
                                }

                            } else {
			    	// App is upto date. Continue to the next activity
                                nextActivity()
                            }
                        }
                    }
                }
        }
    }
```  
## Glide  
When using the *Glide* library to load images, make sure to use global context instead of activity context.  
