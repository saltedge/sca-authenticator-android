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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.databinding.ConnectProviderBinding
import com.saltedge.authenticator.interfaces.OnBackPressListener
import com.saltedge.authenticator.models.ViewModelEvent
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
    View.OnClickListener,
    OnBackPressListener,
    DialogInterface.OnClickListener {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: ConnectProviderViewModel
    private val webViewClient = ConnectWebClient(contract = this)
    private lateinit var binding: ConnectProviderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticatorApp?.appComponent?.inject(this)
        setupViewModel()
        viewModel.setInitialData(
            initialConnectData = arguments?.getSerializable(KEY_CONNECT_DATA) as? ConnectAppLinkData,
            connectionGuid = arguments?.getString(KEY_GUID)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityComponents?.updateAppbar(
            titleResId = viewModel.getTitleResId(),
            backActionImageResId = R.drawable.ic_appbar_action_close
        )
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_connect, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectWebView?.webViewClient = webViewClient
        completeView?.setOnClickListener(this)
        viewModel.onViewCreated()
    }

    override fun onDestroyView() {
        connectWebView?.destroy()
        super.onDestroyView()
    }

    override fun onClick(view: View?) {
        viewModel.onViewClick(view?.id ?: return)
    }

    override fun onClick(listener: DialogInterface?, dialogActionId: Int) {
        viewModel.onDialogActionIdClick(dialogActionId)
    }

    override fun onBackPress(): Boolean {
        return if (viewModel.shouldShowWebView() && connectWebView?.canGoBack() == true) {
            connectWebView.goBack()
            true
        } else {
            false
        }
    }

    override fun webAuthFinishError(errorClass: String, errorMessage: String?) {
        connectWebView?.clearCache(true)
        CookieManager.getInstance().removeSessionCookies(null)
        viewModel.webAuthFinishError(errorClass, errorMessage)
    }

    override fun webAuthFinishSuccess(id: ConnectionID, accessToken: Token) {
        connectWebView?.clearCache(true)
        CookieManager.getInstance().removeSessionCookies(null)
        viewModel.webAuthFinishSuccess(id, accessToken)
    }

    override fun onPageLoadStarted() {
        showLoadProgress()
    }

    override fun onPageLoadFinished() {
        dismissLoadProgress()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(ConnectProviderViewModel::class.java)
        lifecycle.addObserver(viewModel)

        viewModel.onCloseEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let { activity?.finishFragment() }
        })
        viewModel.showErrorAndFinishEvent.observe(this, Observer<ViewModelEvent<String>> {
            it.getContentIfNotHandled()?.let { message ->
                activity?.showErrorDialog(
                    message = message,
                    listener = this
                )
            }
        })
        viewModel.loadUrlInWebViewEvent.observe(this, Observer<ViewModelEvent<String?>> {
            it.getContentIfNotHandled()?.let { url ->
                connectWebView?.loadUrl(url)
            }
        })
        viewModel.iconResId.observe(this, Observer<Int> {
            completeView?.setIconResource(it)
        })
        viewModel.completeTitle.observe(this, Observer<String> {
            completeView?.setTitleText(it)
        })
        viewModel.completeDescription.observe(this, Observer<String> {
            completeView?.setDescription(it)
        })
        viewModel.mainActionTextResId.observe(this, Observer<Int> {
            completeView?.setMainActionText(it)
        })
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
