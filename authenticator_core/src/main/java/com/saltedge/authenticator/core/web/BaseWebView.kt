/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.web

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.webkit.WebSettings
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

fun setThemeMode(context: Context, settings: WebSettings) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            val mode = if (isDarkThemeUsed(context)) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
            WebSettingsCompat.setForceDark(settings, mode)
        }
    } else {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true);
        }
    }
}

private fun isDarkThemeUsed(context: Context): Boolean {
    return context.resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}
