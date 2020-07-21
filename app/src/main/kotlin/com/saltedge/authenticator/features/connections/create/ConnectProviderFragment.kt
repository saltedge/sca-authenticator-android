/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.databinding.ConnectProviderBinding
import com.saltedge.authenticator.interfaces.DialogHandlerListener
import com.saltedge.authenticator.interfaces.OnBackPressListener
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.sdk.constants.KEY_DATA
import com.saltedge.authenticator.sdk.model.ConnectionID
import com.saltedge.authenticator.sdk.model.GUID
import com.saltedge.authenticator.sdk.model.Token
import com.saltedge.authenticator.sdk.model.appLink.ConnectAppLinkData
import com.saltedge.authenticator.sdk.web.ConnectWebClient
import com.saltedge.authenticator.sdk.web.ConnectWebClientContract
import com.saltedge.authenticator.tools.*
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_connect.*
import javax.inject.Inject

class ConnectProviderFragment : BaseFragment(),
    ConnectWebClientContract,
    OnBackPressListener,
    DialogInterface.OnClickListener,
    DialogHandlerListener
{
    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: ConnectProviderViewModel
    private val webViewClient = ConnectWebClient(contract = this)
    private lateinit var binding: ConnectProviderBinding
    private var alertDialog: AlertDialog? = null
    private val safeArgs: ConnectProviderFragmentArgs by navArgs()
    private val guid: String
        get() = safeArgs.guid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticatorApp?.appComponent?.inject(this)
        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityComponents?.updateAppbar(
            titleResId = viewModel.titleRes,
            backActionImageResId = viewModel.backActionIconRes.value
        )
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_connect, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectWebView?.webViewClient = webViewClient
        completeView?.setClickListener(View.OnClickListener { v -> viewModel.onViewClick(v.id) })
    }

    override fun onDestroyView() {
        connectWebView?.destroy()
        super.onDestroyView()
    }

    override fun onClick(listener: DialogInterface?, dialogActionId: Int) {
        viewModel.onDialogActionIdClick(dialogActionId)
    }

    override fun onBackPress(): Boolean {
        return viewModel.onBackPress(webViewCanGoBack = connectWebView?.canGoBack())
    }

    override fun webAuthFinishError(errorClass: String, errorMessage: String?) {
        connectWebView?.clearCache(true)
        CookieManager.getInstance().removeSessionCookies(null)
        viewModel.webAuthFinishError(errorClass, errorMessage)
    }

    override fun webAuthFinishSuccess(id: ConnectionID, accessToken: Token) {
        connectWebView?.clearCache(true)
        CookieManager.getInstance().removeSessionCookies(null)
        viewModel.authFinishedWithSuccess(id, accessToken)
    }

    override fun onPageLoadStarted() {
        showLoadProgress()
    }

    override fun onPageLoadFinished() {
        dismissLoadProgress()
    }

    override fun closeActiveDialogs() {
        if (alertDialog?.isShowing == true) alertDialog?.dismiss()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(ConnectProviderViewModel::class.java)
        lifecycle.addObserver(viewModel)

        viewModel.onCloseEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let { activity?.finishFragment() } //
        })
        viewModel.onShowErrorEvent.observe(this, Observer<ViewModelEvent<String>> {
            it.getContentIfNotHandled()?.let { message ->
                alertDialog = activity?.showErrorDialog(message = message, listener = this)
            }
        })
        viewModel.onUrlChangedEvent.observe(this, Observer<ViewModelEvent<String?>> {
            it.getContentIfNotHandled()?.let { url -> connectWebView?.loadUrl(url) }
        })
        viewModel.goBackEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let { connectWebView.goBack() }
        })
        viewModel.statusIconRes.observe(this, Observer<ResId> {
            completeView?.setIconResource(it)
        })
        viewModel.completeTitle.observe(this, Observer<SpannableString> {
            completeView?.setTitleText(it)
        })
        viewModel.completeDescription.observe(this, Observer<String> {
            completeView?.setDescription(it)
        })
        viewModel.mainActionTextRes.observe(this, Observer<ResId> {
            completeView?.setMainActionText(it)
        })
        viewModel.backActionIconRes.observe(this, Observer<ResId?> {
            activityComponents?.updateAppbar(
                titleResId = viewModel.titleRes,
                backActionImageResId = it
            )
        })

        viewModel.setInitialData(
            initialConnectData = arguments?.getSerializable(KEY_DATA) as? ConnectAppLinkData,
            connectionGuid = arguments?.guid
//            guid
        )
    }

    companion object {
        fun newInstance(connectAppLinkData: ConnectAppLinkData): ConnectProviderFragment {
            return ConnectProviderFragment().apply {
                arguments = Bundle().apply { putSerializable(KEY_DATA, connectAppLinkData) }
            }
        }

        fun newInstance(connectionGuid: GUID): ConnectProviderFragment {
            return ConnectProviderFragment().apply {
                arguments = Bundle().apply { putString(KEY_GUID, connectionGuid) }
            }
        }
    }
}
