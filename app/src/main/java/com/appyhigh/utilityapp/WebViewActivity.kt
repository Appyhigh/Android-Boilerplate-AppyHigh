package com.appyhigh.utilityapp

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import im.delight.android.webview.AdvancedWebView
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.android.synthetic.main.snippet_toolbar_inner.*

class WebViewActivity : AppCompatActivity() {
    val TAG = "WebViewActivity"
    var url = ""
    var title = ""
    private lateinit var mInterstitialAd: InterstitialAd
    var  OPTION_FLAG:Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = getString(R.string.test_interstitial)
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                super.onAdClosed()
                finish()
            }
        }
        title = intent?.getStringExtra("title").toString()
        url = intent?.getStringExtra("url").toString()
        OPTION_FLAG = intent!!.getBooleanExtra("option_flag", false)
        backArrow.setOnClickListener { onBackPressed() }
        toolbarTitle.text = title

        advWebView.setListener(this, object : AdvancedWebView.Listener {
            override fun onPageFinished(url: String?) {
                progressBar.visibility = View.GONE
            }

            override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {
                Log.e(TAG, "onPageError: $description")

                if (URLUtil.isNetworkUrl(failingUrl)) {
                    return
                }
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(failingUrl)
                    startActivity(intent)

                } catch (e: ActivityNotFoundException) {
                    Log.e("AndroiRide", e.toString())
                    //Toast.makeText(this@MainActivity, "No activity found", Toast.LENGTH_LONG).show()
                }
            }

            override fun onDownloadRequested(
                url: String?,
                suggestedFilename: String?,
                mimeType: String?,
                contentLength: Long,
                contentDisposition: String?,
                userAgent: String?
            ) {

            }

            override fun onExternalPageRequest(url: String?) {

            }

            override fun onPageStarted(url: String?, favicon: Bitmap?) {
                progressBar.visibility = View.VISIBLE
            }
        })

        advWebView.loadUrl(url)

    }

    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()
        advWebView.onResume()
    }

    @SuppressLint("NewApi")
    override fun onPause() {
        advWebView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        advWebView.onDestroy()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        advWebView.onActivityResult(requestCode, resultCode, intent)
    }

    override fun onBackPressed() {
        if (mInterstitialAd.isLoaded)
            mInterstitialAd.show()
        if (OPTION_FLAG) {
            openMainActivity()
        } else {
            finish()
        }
    }
    private fun openMainActivity() {
        val mainIntent = Intent(this@WebViewActivity, MainActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(mainIntent)
        finish()
    }
}
