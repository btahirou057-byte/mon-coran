package com.moncoran.app

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ID de TEST de Google : à remplacer par votre vrai ID AdMob après la publication
internal const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

private var adsReady by mutableStateOf(false)

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (!adsReady) {
            withContext(Dispatchers.IO) {
                MobileAds.initialize(context)
            }
            adsReady = true
        }
    }

    if (adsReady) {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = BANNER_AD_UNIT_ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
