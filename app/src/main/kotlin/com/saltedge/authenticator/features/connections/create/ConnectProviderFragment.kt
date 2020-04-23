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
package com.saltedge.authenticator.features.connections.create

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.features.connections.create.di.ConnectProviderModule
import com.saltedge.authenticator.interfaces.OnBackPressListener
import com.saltedge.authenticator.interfaces.UpActionImageListener
import com.saltedge.authenticator.sdk.model.ConnectionID
import com.saltedge.authenticator.sdk.model.GUID
import com.saltedge.authenticator.sdk.model.Token
import com.saltedge.authenticator.sdk.model.appLink.ConnectAppLinkData
import com.saltedge.authenticator.sdk.web.ConnectWebClient
import com.saltedge.authenticator.sdk.web.ConnectWebClientContract
import com.saltedge.authenticator.tool.*
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_connect.*
import javax.inject.Inject

class ConnectProviderFragment : BaseFragment(),
    ConnectProviderContract.View,
    ConnectWebClientContract,
    View.OnClickListener,
    OnBackPressListener,
    UpActionImageListener {

    @Inject
    lateinit var presenterContract: ConnectProviderContract.Presenter
    private val webViewClient = ConnectWebClient(contract = this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        presenterContract.setInitialData(
            initialConnectData = arguments?.getSerializable(KEY_CONNECT_DATA) as? ConnectAppLinkData,
            connectionGuid = arguments?.getString(KEY_GUID)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityComponents?.updateAppbarTitle(getString(presenterContract.getTitleResId()))
        return inflater.inflate(R.layout.fragment_connect, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectWebView?.webViewClient = webViewClient
        completeView?.setOnClickListener(this)
        updateViewsContent()
        presenterContract.viewContract = this
        presenterContract.onViewCreated()
    }

    override fun onDestroyView() {
        connectWebView?.destroy()
        presenterContract.viewContract = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        presenterContract.onDestroyView()
        super.onDestroy()
    }

    override fun onBackPress(): Boolean {
        return if (presenterContract.shouldShowWebView && connectWebView?.canGoBack() == true) {
            connectWebView.goBack()
            true
        } else {
            false
        }
    }

    override fun onClick(view: View?) {
        presenterContract.onViewClick(view?.id ?: return)
    }

    override fun closeView() {
        activity?.finishFragment()
    }

    override fun updateViewsContent() {
        completeView?.setIconResource(presenterContract.iconResId)
        completeView?.setTitleText(presenterContract.completeTitle)
        completeView?.setSubtitleText(presenterContract.completeMessage)
        completeView?.setMainActionText(presenterContract.mainActionTextResId)
        completeView?.setAltActionText(presenterContract.reportProblemActionText)

        updateLayoutsVisibility()
    }

    override fun loadUrlInWebView(url: String) {
        connectWebView?.loadUrl(url)
    }

    override fun webAuthFinishError(errorClass: String, errorMessage: String?) {
        connectWebView?.clearCache(true)
        CookieManager.getInstance().removeSessionCookies(null)
        presenterContract.webAuthFinishError(errorClass, errorMessage)
    }

    override fun webAuthFinishSuccess(id: ConnectionID, accessToken: Token) {
        connectWebView?.clearCache(true)
        CookieManager.getInstance().removeSessionCookies(null)
        presenterContract.webAuthFinishSuccess(id, accessToken)
    }

    override fun onPageLoadStarted() {
        showLoadProgress()
    }

    override fun onPageLoadFinished() {
        dismissLoadProgress()
    }

    override fun showErrorAndFinish(message: String) {
        activity?.showErrorDialog(
            message = message,
            listener = DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                activity?.finishFragment()
            }
        )
    }

    override fun getUpActionImageResId(): ResId? = R.drawable.ic_close_white_24dp

    private fun updateLayoutsVisibility() {
        fragmentConnectProcessingLayout?.setVisible(show = presenterContract.shouldShowProgressView)
        completeView?.setVisible(show = presenterContract.shouldShowCompleteView)
        connectWebView?.setVisible(show = presenterContract.shouldShowWebView)
    }

    private fun injectDependencies() {
        authenticatorApp?.appComponent?.addConnectProviderModule(ConnectProviderModule())?.inject(
            this
        )
    }

    companion object {
        const val KEY_CONNECT_DATA = "KEY_CONNECT_DATA"

        fun newInstance(connectAppLinkData: ConnectAppLinkData): ConnectProviderFragment {
            return ConnectProviderFragment().apply {
                arguments = Bundle().apply { putSerializable(KEY_CONNECT_DATA, connectAppLinkData) }
            }
        }

        fun newInstance(connectionGuid: GUID): ConnectProviderFragment {
            return ConnectProviderFragment().apply {
                arguments = Bundle().apply { putString(KEY_GUID, connectionGuid) }
            }
        }
    }
}
