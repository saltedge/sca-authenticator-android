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
package com.saltedge.authenticator.widget.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.saltedge.authenticator.R
import com.saltedge.authenticator.sdk.constants.KEY_TITLE
import kotlinx.android.synthetic.main.fragment_web_view.*

class WebViewFragment : BaseFragment() {

    private var url = ""
    private var title = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            url = it.getString(KEY_URL, "")
            title = it.getString(KEY_TITLE, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityComponents?.updateAppbar(
            title = title,
            actionImageResId = R.drawable.ic_appbar_action_back
        )
        return inflater.inflate(R.layout.fragment_web_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customWebView?.webViewClient = webViewClient
        customWebView?.loadUrl(url)
    }

    private val webViewClient = object : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            showLoadProgress()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            dismissLoadProgress()
        }
    }

    companion object {
        const val KEY_URL = "KEY_URL"

        fun newBundle(url: String = "", title: String): Bundle {
            return Bundle().apply {
                putString(KEY_URL, url)
                putString(KEY_TITLE, title)
            }
        }

        fun newInstance(url: String = "", title: String): WebViewFragment {
            return newInstance(newBundle(url, title))
        }

        fun newInstance(args: Bundle): WebViewFragment {
            return WebViewFragment().apply { arguments = args }
        }
    }
}
