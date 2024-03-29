/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2019 Salt Edge Inc.
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
package com.saltedge.authenticator.sdk.web

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.webkit.WebView
import com.saltedge.authenticator.sdk.AuthenticatorApiManager

class SEWebView : WebView {

    constructor(context: Context) : super(context.applicationContext) {
        setupView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context.applicationContext, attrs) {
        setupView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
        super(context.applicationContext, attrs, defStyleAttr) {
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
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.userAgentString = AuthenticatorApiManager.userAgentInfo
    }
}
