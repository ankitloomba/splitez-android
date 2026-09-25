package com.splitezapp.ui.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlin.random.Random

object InterstitialAdManager {
    private const val AD_UNIT_ID = "ca-app-pub-2574042432872288~3272890896"

    private var interstitialAd: InterstitialAd? = null
    private var isInitialized = false
    private var showCount = 0
    private val maxShowsPerDay = Random.nextInt(1, 4) // 1 to 3 times per day
    private var lastResetDay = -1

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true
        MobileAds.initialize(context) {}
        loadAd(context)
    }

    private fun loadAd(context: Context) {
        InterstitialAd.load(
            context,
            AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun showIfReady(activity: Activity) {
        val today = java.time.LocalDate.now().dayOfYear
        if (today != lastResetDay) {
            showCount = 0
            lastResetDay = today
        }

        if (showCount >= maxShowsPerDay) return

        val ad = interstitialAd ?: return
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                loadAd(activity)
            }
        }

        ad.show(activity)
        showCount++
    }
}
