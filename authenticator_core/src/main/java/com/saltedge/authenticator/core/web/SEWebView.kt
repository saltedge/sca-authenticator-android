/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.web

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.webkit.WebView

class SEWebView : WebView {

    constructor(context: Context) : super(getFixedContext(context)) {
        setupView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(getFixedContext(context), attrs) {
        setupView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        getFixedContext(
            context
        ), attrs, defStyleAttr
    ) {
        setupView()
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo?): InputConnection? {
        val inputConnection = super.onCreateInputConnection(outAttrs)
        if (outAttrs != null) {
            outAttrs.imeOptions = outAttrs.imeOptions and EditorInfo.IME_ACTION_GO.inv()
            outAttrs.imeOptions = outAttrs.imeOptions and EditorInfo.IME_ACTION_SEARCH.inv()
            outAttrs.imeOptions = outAttrs.imeOptions and EditorInfo.IME_ACTION_SEND.inv()
            outAttrs.imeOptions = outAttrs.imeOptions and EditorInfo.IME_ACTION_DONE.inv()
            outAttrs.imeOptions = outAttrs.imeOptions and EditorInfo.IME_ACTION_NONE.inv()
            outAttrs.imeOptions = outAttrs.imeOptions or EditorInfo.IME_ACTION_NEXT
        }
        return inputConnection
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupView() {
        this.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        setThemeMode(context, this.settings)
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.userAgentString = "" //TODO ClientConfig.userAgentInfo
    }
}

fun getFixedContext(context: Context): Context {
    return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP
        || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
        context.createConfigurationContext(Configuration())
    } else context
}
