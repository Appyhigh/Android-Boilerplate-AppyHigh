package com.appyhigh.utilityapp

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.appyhigh.utilityapp.utils.SharedPrefUtil
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class SplashActivity : AppCompatActivity() {
    val TAG = "SplashActivity"
    var showAds = true
    var showAdsString = "yes"
    private lateinit var mInterstitialAd: InterstitialAd
    lateinit var sharedPrefUtil: SharedPrefUtil
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash)

        sharedPrefUtil = SharedPrefUtil(this)
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = getString(R.string.test_interstitial)

        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                super.onAdClosed()
                checkForLatestVersion()
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                mInterstitialAd.show()
            }

            override fun onAdFailedToLoad(p0: Int) {
                super.onAdFailedToLoad(p0)
                checkForLatestVersion()
            }
        }
        getRemoteConfig()
    }

    private fun getRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(420)
            .build()
        remoteConfig.setConfigSettings(configSettings)
        remoteConfig.setDefaults(R.xml.remote_config)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d(TAG, "Config params updated: $updated")
                    remoteConfig.activate()
                }
                showAdsString = remoteConfig.getString("display_ads")
                showAds = showAdsString == "yes"
                Log.e(TAG, "getRemoteConfig: $showAds")
                Log.e(TAG, "getRemoteConfig: $showAdsString")
                sharedPrefUtil.saveBoolean("ADS", showAds)
                if (showAds) {
                    Log.e(TAG, "getRemoteConfig: SHOW")
                    mInterstitialAd.loadAd(AdRequest.Builder().build())
                } else {
                    Log.e(TAG, "getRemoteConfig: DO NOT SHOW")
                    checkForLatestVersion()
                }
            }
    }


    private fun nextActivity() {
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
    }

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
                                nextActivity()
                            }
                        }
                    }
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 212) run {
            if (resultCode != RESULT_OK) {
                checkForLatestVersion()
            } else {
                nextActivity()
            }
        }
    }
}
