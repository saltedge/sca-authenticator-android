/*
 * Copyright (c) 2021 Salt Edge Inc.
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
        setThemeMode(context, this.settings)
    }
}
