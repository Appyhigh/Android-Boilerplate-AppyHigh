package com.appyhigh.utilityapp.events

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import com.clevertap.android.sdk.CleverTapAPI
import com.google.firebase.analytics.FirebaseAnalytics
import java.io.File
import java.util.*

object AnalyticsManager {
    private val binaryPlaces = arrayOf(
        "/data/bin/", "/system/bin/", "/system/xbin/", "/sbin/",
        "/data/local/xbin/", "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/",
        "/data/local/"
    )
    private var sAppContext: Context? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private var cleverTapDefaultInstance: CleverTapAPI? = null
    private fun canSend(): Boolean {
        return sAppContext != null && mFirebaseAnalytics != null
    }

    private fun canPush(): Boolean {
        return sAppContext != null && cleverTapDefaultInstance != null
    }

    @Synchronized
    fun initialize(context: Context) {
        sAppContext = context
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(context)
        setProperty("DeviceType", getDeviceType(context))
        setProperty(
            "Rooted", isRooted.toString()
        )
    }

    private fun setProperty(
        propertyName: String,
        propertyValue: String
    ) {
        if (canSend()) {
            mFirebaseAnalytics!!.setUserProperty(propertyName, propertyValue)
        }
    }

    fun logEvent(eventName: String?) {
        if (canSend()) {
            mFirebaseAnalytics!!.logEvent(eventName!!, Bundle())
            pushCTEvent(eventName)
        }
    }

    fun logEvent(eventName: String?, params: Bundle) {
        if (canSend()) {
            mFirebaseAnalytics!!.logEvent(eventName!!, params)
            pushCTEventWithParams(eventName, bundleToMap(params))
        }
    }

    fun bundleToMap(extras: Bundle): HashMap<String?, Any?> {
        val map =
            HashMap<String?, Any?>()
        val ks = extras.keySet()
        for (key in ks) {
            map[key] = extras.getString(key)
        }
        return map
    }

    fun setCurrentScreen(activity: Activity?, screenName: String?) {
        if (canSend()) {
            if (null != screenName) {
                mFirebaseAnalytics!!.setCurrentScreen(
                    activity!!,
                    screenName,
                    screenName
                )
                cleverTapDefaultInstance!!.recordScreen(screenName)
            }
        }
    }

    private fun getDeviceType(c: Context): String {
        val uiModeManager =
            c.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        val modeType = uiModeManager.currentModeType
        return when (modeType) {
            Configuration.UI_MODE_TYPE_TELEVISION -> "TELEVISION"
            Configuration.UI_MODE_TYPE_WATCH -> "WATCH"
            Configuration.UI_MODE_TYPE_NORMAL -> if (isTablet(
                    c
                )
            ) "TABLET" else "PHONE"
            Configuration.UI_MODE_TYPE_UNDEFINED -> "UNKOWN"
            else -> ""
        }
    }

    private val isRooted: Boolean
        get() {
            for (p in binaryPlaces) {
                val su = File(p + "su")
                if (su.exists()) {
                    return true
                }
            }
            return false
        }

    private fun isTablet(context: Context): Boolean {
        return context.resources.configuration.smallestScreenWidthDp >= 600
    }

    fun pushCTEvent(eventName: String?) {
        if (canPush()) {
            cleverTapDefaultInstance!!.pushEvent(eventName)
        }
    }

    fun pushCTEventWithParams(
        eventName: String?,
        hashMap: HashMap<String?, Any?>?
    ) {
        if (canPush()) {
            cleverTapDefaultInstance!!.pushEvent(eventName, hashMap)
        }
    }

    fun pushCTProfile(eventName: HashMap<String?, Any?>?) {
        cleverTapDefaultInstance!!.pushProfile(eventName)
    }
}