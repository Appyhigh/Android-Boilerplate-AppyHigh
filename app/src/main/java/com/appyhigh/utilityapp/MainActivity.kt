package com.appyhigh.utilityapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import com.appyhigh.utilityapp.Utils.Constants
import com.appyhigh.utilityapp.Utils.RateDialog
import com.appyhigh.utilityapp.Utils.SharedPrefUtil
import com.facebook.ads.*
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_fb_nativead.view.*
import kotlinx.android.synthetic.main.layout_nativead_small.view.*
import java.util.ArrayList
import android.widget.Toast
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.menu.MenuBuilder
import kotlinx.android.synthetic.main.snippet_toolbar_home.*


class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    lateinit var sharedPrefUtil: SharedPrefUtil
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var fbNativeAd : NativeAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(homeToolBar)
        checkForNotifications()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        sharedPrefUtil = SharedPrefUtil(this)
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = getString(R.string.main_exit_id)
        mInterstitialAd.loadAd(AdRequest.Builder().build())

        mInterstitialAd.adListener = object : AdListener(){
            override fun onAdClosed() {
                super.onAdClosed()
                mInterstitialAd.loadAd(AdRequest.Builder().build())
                showExitPopup()
            }

            override fun onAdClicked() {
                super.onAdClicked()
                val abTestBundle = Bundle()
                abTestBundle.putString("app_version", BuildConfig.VERSION_NAME)
                firebaseAnalytics.logEvent("home_exit_intr", abTestBundle)
                firebaseAnalytics.logEvent("ad_clicked", abTestBundle)
            }
        }
        loadNativeBannerSmall()
        showRateDialog()
        loadFbNativeAd()
    }

    private fun loadFbNativeAd(){
        fbNativeAd = NativeAd(this,getString(R.string.fb_native_ad))
        fbNativeAd.setAdListener(object :NativeAdListener{
            override fun onAdClicked(p0: Ad?) {

            }

            override fun onMediaDownloaded(p0: Ad?) {

            }

            override fun onError(p0: Ad?, p1: AdError?) {
                Log.e(TAG, "onError: ${p1!!.errorMessage}")
                loadNativeBannerBig()
            }

            override fun onAdLoaded(p0: Ad?) {
                inflateFbAdView(fbNativeAd)
            }

            override fun onLoggingImpression(p0: Ad?) {

            }
        })
        fbNativeAd.loadAd()
    }

    private fun inflateFbAdView(nativeAd: NativeAd){
        nativeAd.unregisterView()
        val cardView = LayoutInflater.from(this).inflate(R.layout.item_fb_nativead, null) as CardView
        nativeAdFb.addView(cardView)

        val nativeAdLayout = cardView.native_ad_container as NativeAdLayout

        // Add the AdOptionsView
        val adChoicesContainer = cardView.ad_choices_container as LinearLayout
        val adOptionsView = AdOptionsView(this@MainActivity, nativeAd, nativeAdLayout)
        adChoicesContainer.removeAllViews()
        adChoicesContainer.addView(adOptionsView, 0)

        val nativeAdIcon = cardView.native_ad_icon as AdIconView
        val nativeAdTitle = cardView.native_ad_title as TextView
        val nativeAdMedia = cardView.native_ad_media as MediaView
        val nativeAdSocialContext = cardView.native_ad_social_context as TextView
        val nativeAdBody = cardView.native_ad_body as TextView
        val sponsoredLabel = cardView.native_ad_sponsored_label as TextView
        val nativeAdCallToAction = cardView.native_ad_call_to_action as Button

        nativeAdTitle.text = nativeAd.advertiserName
        nativeAdBody.text = nativeAd.adBodyText
        nativeAdSocialContext.text = nativeAd.adSocialContext
        nativeAdCallToAction.visibility = if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
        nativeAdCallToAction.text = nativeAd.adCallToAction
        sponsoredLabel.text = nativeAd.sponsoredTranslation

        val clickableViews = ArrayList<View>()
        clickableViews.add(nativeAdTitle)
        clickableViews.add(nativeAdCallToAction)

        nativeAd.registerViewForInteraction(cardView, nativeAdMedia, nativeAdIcon, clickableViews)
    }

    private fun checkForNotifications(){
        try {
            val which = intent.getStringExtra("which")
            val link = intent.getStringExtra("link")
            val title = intent.getStringExtra("title")

            when(which){
                "P"->{
                    try {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=$link")
                            )
                        )
                    } catch (anfe: android.content.ActivityNotFoundException) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=$link")
                            )
                        )
                    }
                }

                "B"->{
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                    } catch (anfe: android.content.ActivityNotFoundException) {
                        Toast.makeText(this@MainActivity,"Unable to open the link",Toast.LENGTH_LONG).show()
                    }
                }

                "L"->{
                    val i = Intent(this, WebViewActivity::class.java)
                    i.putExtra("url", link)
                    i.putExtra("title", title)
                    startActivity(i)
                }
            }
        }catch ( e : Exception){
            Log.e(TAG, "checkForNotifications: $e")
        }
    }

    override fun onBackPressed() {
        if(mInterstitialAd.isLoaded)
            mInterstitialAd.show()
        else
            showExitPopup()
    }

    private fun loadNativeBannerBig(){
        val adLoader = AdLoader.Builder(this, getString(R.string.native_id))
            .forUnifiedNativeAd { unifiedNativeAd : UnifiedNativeAd ->
                val adView = layoutInflater
                    .inflate(R.layout.layout_nativead_big, null) as UnifiedNativeAdView
                populateUnifiedNativeAdView(unifiedNativeAd, adView)
                nativeAdFb.removeAllViews()
                nativeAdFb.addView(adView)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(errorCode: Int) {
                    // Handle the failure by logging, altering the UI, and so on.

                }
                override fun onAdClicked() {
                    super.onAdClicked()
                    val abTestBundle = Bundle()
                    firebaseAnalytics.logEvent("bookmark_native", abTestBundle)
                    firebaseAnalytics.logEvent("ad_clicked", abTestBundle)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }
    private fun loadNativeBannerSmall(){
        val adLoader = AdLoader.Builder(this, getString(R.string.native_id))
            .forUnifiedNativeAd { unifiedNativeAd : UnifiedNativeAd ->
                val adView = layoutInflater
                    .inflate(R.layout.layout_nativead_small, null) as UnifiedNativeAdView
                populateUnifiedNativeAdView(unifiedNativeAd, adView)
                nativeAdArea.removeAllViews()
                nativeAdArea.addView(adView)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(errorCode: Int) {
                    // Handle the failure by logging, altering the UI, and so on.

                }
                override fun onAdClicked() {
                    super.onAdClicked()
                    val abTestBundle = Bundle()
                    firebaseAnalytics.logEvent("bookmark_native", abTestBundle)
                    firebaseAnalytics.logEvent("ad_clicked", abTestBundle)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateUnifiedNativeAdView(nativeAd: UnifiedNativeAd, adView: UnifiedNativeAdView) {
        val iconView = adView.ad_icon as ImageView
        Log.e("nativead", "ad body : " + nativeAd.body)

        val icon = nativeAd.icon
        adView.iconView = iconView
        if (icon == null) {
            adView.iconView.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(icon.drawable)
            adView.iconView.visibility = View.VISIBLE
        }

        val ratingBar = adView.ad_stars as View
        adView.starRatingView = ratingBar
        if (nativeAd.starRating == null) {
            adView.starRatingView.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        val adHeadline = adView.ad_headline as TextView
        adView.headlineView = adHeadline
        (adView.headlineView as TextView).text = nativeAd.headline

        val adBody = adView.ad_body as TextView
        adView.bodyView = adBody
        (adView.bodyView as TextView).text = nativeAd.body

        val cta = adView.ad_call_to_action as Button
        adView.callToActionView = cta
        (adView.callToActionView as Button).text = nativeAd.callToAction

        val price = adView.ad_price as TextView
        adView.priceView = price
        if (nativeAd.price == null) {
            adView.priceView.visibility = View.GONE
        } else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        val playAttribution = adView.playAttribution as ImageView
        if (nativeAd.price != null && nativeAd.starRating != null) {
            playAttribution.visibility = View.VISIBLE
        } else {
            playAttribution.visibility = View.GONE
        }

        adView.setNativeAd(nativeAd)
    }

    private fun showExitPopup() {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to exit?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                this@MainActivity.finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showRateDialog(){
        Handler().postDelayed({
            var times = 0
            if(!sharedPrefUtil.getBoolean(Constants.RATED,false)){
                times = (sharedPrefUtil.getInt(Constants.TIMES, 0) + 1) % 5
                sharedPrefUtil.saveInt(Constants.TIMES,times)
                if(times==0){
                    val rateDialog = RateDialog()
                    rateDialog.show(supportFragmentManager,"RATEDIALOG")
                }
            }
        },500)
    }


    fun shareApp() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Hey, I found this cool app on playstore, check it out: http://bit.ly/appy-qrcode"
        )
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.popup_menu, menu)
        menu as MenuBuilder
        menu.setOptionalIconsVisible(true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return when (id) {
            R.id.share -> {
                shareApp()
                true
            }
            R.id.rateUs -> {
                val rateDialog = RateDialog()
                rateDialog.show(supportFragmentManager,"RATEDIALOG")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
