package com.splitezapp.ui.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.splitezapp.data.api.ApiClient
import com.splitezapp.data.models.AdPlacement

/**
 * Server-driven ad banner slot. Fetches placement config from the backend
 * and renders the ad. Replace the placeholder body with actual AdMob
 * (com.google.android.gms:play-services-ads) AdView once the SDK is added.
 *
 * Integration steps:
 * 1. Add play-services-ads dependency to build.gradle
 * 2. Add AdMob App ID to AndroidManifest.xml
 * 3. Replace the placeholder Box with an AdView composable
 */
@Composable
fun AdBannerSlot(
    screen: String,
    placementName: String,
    isAdFree: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (isAdFree) return

    var placement by remember { mutableStateOf<AdPlacement?>(null) }

    LaunchedEffect(screen, placementName) {
        try {
            val placements = ApiClient.api.getAdPlacements(screen)
            placement = placements.firstOrNull { it.name == placementName }
        } catch (_: Exception) { /* ads are non-critical */ }
    }

    placement?.let { p ->
        val adUnitId = p.adUnitAndroid ?: return@let

        // TODO: Replace with actual AdView from Google Mobile Ads SDK
        // val adView = AdView(context).apply {
        //     setAdSize(AdSize.BANNER)
        //     adUnitId = adUnitId
        //     loadAd(AdRequest.Builder().build())
        // }
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Ad",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
