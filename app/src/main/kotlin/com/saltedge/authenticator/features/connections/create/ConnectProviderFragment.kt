/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.connections.create

import android.Manifest
import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.app.authenticatorApp
import com.saltedge.authenticator.app.guid
import com.saltedge.authenticator.core.api.KEY_API_VERSION
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.model.ConnectAppLinkData
import com.saltedge.authenticator.core.web.ConnectWebClient
import com.saltedge.authenticator.core.web.ConnectWebClientContract
import com.saltedge.authenticator.databinding.FragmentConnectBinding
import com.saltedge.authenticator.interfaces.DialogHandlerListener
import com.saltedge.authenticator.interfaces.OnBackPressListener
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManager
import com.saltedge.authenticator.sdk.v2.config.ApiV2Config
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.popBackStack
import com.saltedge.authenticator.tools.showErrorDialog
import com.saltedge.authenticator.widget.fragment.BaseFragment
import javax.inject.Inject

class ConnectProviderFragment : BaseFragment(),
    ConnectWebClientContract,
    OnBackPressListener,
    DialogInterface.OnClickListener,
    DialogHandlerListener
{
    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: ConnectProviderViewModel
    private val webViewClient = ConnectWebClient(
        authenticationReturnUrl = ApiV2Config.authenticationReturnUrl,
        contract = this
    )
    private var binding: FragmentConnectBinding? = null
    private var alertDialog: AlertDialog? = null
    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { actionMap ->
            when (actionMap.key) {
                Manifest.permission.ACCESS_FINE_LOCATION -> {
                    if (actionMap.value) {
                        viewModel.updateLocationStateOfConnection()
                    }
                }
                Manifest.permission.ACCESS_COARSE_LOCATION -> {
                    if (actionMap.value) {
                        viewModel.updateLocationStateOfConnection()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.authenticatorApp?.appComponent?.inject(this)
        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityComponents?.updateAppbar(
            titleResId = viewModel.titleRes,
            backActionImageResId = viewModel.backActionIconRes.value
        )
        binding = FragmentConnectBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.connectWebView?.webViewClient = webViewClient
        binding?.connectWebView?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.theme_background))
        binding?.completeView?.setClickListener { v -> viewModel.onViewClick(v.id) }
    }

    override fun onDestroyView() {
        binding?.connectWebView?.destroy()
        binding = null
        super.onDestroyView()
    }

    override fun onClick(listener: DialogInterface?, dialogActionId: Int) {
        viewModel.onDialogActionIdClick(dialogActionId)
    }

    override fun onBackPress(): Boolean {
        return viewModel.onBackPress(webViewCanGoBack = binding?.connectWebView?.canGoBack())
    }

    override fun onReturnToRedirect(url: String) {
        binding?.connectWebView?.clearCache(true)
        CookieManager.getInstance().removeSessionCookies(null)
        viewModel.onReturnToRedirect(url)
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
        val appLinkData = arguments?.getSerializable(KEY_DATA) as? ConnectAppLinkData
        val apiVersion = arguments?.getString(KEY_API_VERSION)
        viewModelFactory.setScaApiVersion(apiVersion)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ConnectProviderViewModel::class.java)
        lifecycle.addObserver(viewModel)

        viewModel.webViewVisibility.observe(this, Observer<Int> { visibility ->
            binding?.connectWebView?.visibility = visibility
        })

        viewModel.progressViewVisibility.observe(this, Observer<Int> { visibility ->
            binding?.fragmentConnectProcessingLayout?.root?.visibility = visibility
        })

        viewModel.completeViewVisibility.observe(this, Observer<Int> { visibility ->
            binding?.completeView?.visibility = visibility
        })

        viewModel.onCloseEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let { popBackStack() }
        })
        viewModel.onShowErrorEvent.observe(this, Observer<ViewModelEvent<String>> {
            it.getContentIfNotHandled()?.let { message ->
                alertDialog = activity?.showErrorDialog(message = message, listener = this)
            }
        })
        viewModel.onUrlChangedEvent.observe(this, Observer<ViewModelEvent<String?>> {
            it.getContentIfNotHandled()?.let { url -> binding?.connectWebView?.loadUrl(url) }
        })
        viewModel.goBackEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let { binding?.connectWebView?.goBack() }
        })
        viewModel.statusIconRes.observe(this, Observer<ResId> {
            binding?.completeView?.setIconResource(it)
        })
        viewModel.completeTitle.observe(this, Observer<SpannableString> {
            binding?.completeView?.setTitleText(it)
        })
        viewModel.completeDescription.observe(this, Observer<String> {
            binding?.completeView?.setDescription(it)
        })
        viewModel.mainActionTextRes.observe(this, Observer<ResId> {
            binding?.completeView?.setMainActionText(it)
        })
        viewModel.backActionIconRes.observe(this, Observer<ResId?> {
            activityComponents?.updateAppbar(titleResId = viewModel.titleRes, backActionImageResId = it)
        })
        viewModel.onAskPermissionsEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let {
                requestMultiplePermissions.launch(DeviceLocationManager.permissions)
            }
        })
        viewModel.setInitialData(initialConnectData = appLinkData, connectionGuid = arguments?.guid)
    }
}
