/*
 * Copyright (c) 2019 Salt Edge Inc.
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
import com.saltedge.authenticator.core.api.KEY_TITLE
import com.saltedge.authenticator.databinding.FragmentWebViewBinding

class WebViewFragment : BaseFragment() {

    private var url = ""
    private var title = ""
    private lateinit var binding: FragmentWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            url = it.getString(KEY_URL, "")
            title = it.getString(KEY_TITLE, "")
        }
    }

    override fun onResume() {
        super.onResume()
        binding.customWebView.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityComponents?.updateAppbar(
            title = title,
            backActionImageResId = R.drawable.ic_appbar_action_back
        )
        binding = FragmentWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.customWebView.webViewClient = webViewClient
        binding.customWebView.loadUrl(url)
    }

    override fun onPause() {
        super.onPause()
        binding.customWebView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.customWebView.stopLoading()
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
    }
}
