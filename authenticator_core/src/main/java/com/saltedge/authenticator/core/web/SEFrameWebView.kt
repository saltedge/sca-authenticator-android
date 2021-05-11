/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 or later.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * For the additional permissions granted for Salt Edge Authenticator
 * under Section 7 of the GNU General Public License see THIRD_PARTY_NOTICES.md
 */
package com.saltedge.authenticator.core.web

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import timber.log.Timber

/**
 * Web view designated for light content (Authorization content)
 */
open class SEFrameWebView : WebView {

    constructor(context: Context) : super(getFixedContext(context)) {
        setupView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(getFixedContext(context), attrs) {
        setupView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(getFixedContext(context), attrs, defStyleAttr) {
        setupView()
    }

    override fun setOverScrollMode(mode: Int) {
        runCatching {
            super.setOverScrollMode(mode)
        }.onFailure {
            Timber.e(it)
        }
    }

    private fun setupView() {
        this.setLayerType(LAYER_TYPE_HARDWARE, null)
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            val mode = if (isDarkThemeUsed(context)) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
            WebSettingsCompat.setForceDark(this.settings, mode)
        }
    }

    private fun isDarkThemeUsed(context: Context): Boolean {
        return context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    companion object {
        // To fix Android Lollipop WebView problem create a new configuration on that Android version only
        private fun getFixedContext(context: Context): Context {
            return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP
                || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
                context.createConfigurationContext(Configuration())
            } else context
        }
    }
}
