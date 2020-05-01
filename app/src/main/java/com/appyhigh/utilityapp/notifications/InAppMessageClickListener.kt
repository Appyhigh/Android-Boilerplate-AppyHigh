package com.appyhigh.utilityapp.notifications

import android.content.Context
import android.util.Log
import com.google.firebase.inappmessaging.FirebaseInAppMessagingClickListener
import com.google.firebase.inappmessaging.model.Action
import com.google.firebase.inappmessaging.model.InAppMessage

class InAppMessageClickListener(private val context: Context) :
    FirebaseInAppMessagingClickListener {
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

}