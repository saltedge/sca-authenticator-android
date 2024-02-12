/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.authorizations.details

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.*
import com.saltedge.authenticator.cloud.clearNotifications
import com.saltedge.authenticator.core.api.KEY_ID
import com.saltedge.authenticator.core.api.KEY_TITLE
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.databinding.FragmentAuthorizationDetailsBinding
import com.saltedge.authenticator.databinding.FragmentAuthorizationsListBinding
import com.saltedge.authenticator.databinding.FragmentConnectionsListBinding
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.interfaces.DialogHandlerListener
import com.saltedge.authenticator.interfaces.OnBackPressListener
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManager
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.tools.getErrorMessage
import com.saltedge.authenticator.tools.popBackStack
import com.saltedge.authenticator.tools.showInfoDialog
import com.saltedge.authenticator.widget.fragment.BaseFragment
import java.util.*
import javax.inject.Inject

class AuthorizationDetailsFragment : BaseFragment(),
    View.OnClickListener,
    OnBackPressListener,
    DialogHandlerListener {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: AuthorizationDetailsViewModel
    private lateinit var binding: FragmentAuthorizationDetailsBinding
    private var timeViewUpdateTimer: Timer = Timer()
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
        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityComponents?.updateAppbar(
            titleResId = viewModel.titleRes,
            backActionImageResId = R.drawable.ic_appbar_action_close
        )
        binding = FragmentAuthorizationDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.contentView.setActionClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        activity?.clearNotifications()
        startTimer()
    }

    override fun onPause() {
        stopTimer()
        super.onPause()
    }

    override fun onBackPress(): Boolean {
        return viewModel.onBackPress()
    }

    override fun onClick(view: View?) {
        viewModel.onViewClick(itemViewId = view?.id ?: return)
    }

    override fun closeActiveDialogs() {
        if (alertDialog?.isShowing == true) alertDialog?.dismiss()
    }

    private fun startTimer() {
        timeViewUpdateTimer = Timer()
        timeViewUpdateTimer.schedule(object : TimerTask() {
            override fun run() { activity?.runOnUiThread { viewModel.onTimerTick() } }
        }, 0, TIME_VIEW_UPDATE_TIMEOUT)
    }

    private fun stopTimer() {
        timeViewUpdateTimer.cancel()
        timeViewUpdateTimer.purge()
    }

    private fun setupViewModel() {
        authenticatorApp?.appComponent?.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(AuthorizationDetailsViewModel::class.java)
        viewModel.setInitialData(
            identifier = arguments?.getSerializable(KEY_ID) as? AuthorizationIdentifier,
            closeAppOnBackPress = arguments?.getBoolean(KEY_CLOSE_APP, true),
            titleRes = arguments?.getInt(KEY_TITLE, R.string.authorization_feature_title)
        )
        viewModel.bindLifecycleObserver(lifecycle = lifecycle)

        viewModel.onTimeUpdateEvent.observe(this, Observer<ViewModelEvent<Unit>> { event ->
            event.getContentIfNotHandled()?.let {
                binding.headerView.onTimeUpdate()
            }
        })
        viewModel.onErrorEvent.observe(this, Observer<ViewModelEvent<ApiErrorData>> { event ->
            event.getContentIfNotHandled()?.let { error ->
                view?.let { anchor ->
                    val message = error.getErrorMessage(anchor.context)
                    Snackbar.make(anchor, message, Snackbar.LENGTH_LONG).show()
                }
            }
        })
        viewModel.onCloseAppEvent.observe(this, Observer<ViewModelEvent<Unit>> { event ->
            event.getContentIfNotHandled()?.let { activity?.finish() }
        })
        viewModel.onCloseViewEvent.observe(this, Observer<ViewModelEvent<Unit>> { event ->
            event.getContentIfNotHandled()?.let { popBackStack() }
        })
        viewModel.authorizationModel.observe(this, Observer<AuthorizationItemViewModel> {
            binding.headerView.setTitleAndLogo(title = it.connectionName, logoUrl = it.connectionLogoUrl ?: "")
            binding.headerView.setProgressTime(startTime = it.startTime, endTime = it.endTime)
            binding.headerView.ignoreTimeUpdate = it.ignoreTimeUpdate
            binding.headerView.visibility = it.timeViewVisibility
            binding.contentView.setTitleAndDescription(it.title, it.description)
            binding.contentView.setViewMode(it.status)
        })
        viewModel.onRequestPermissionEvent.observe(this, Observer { event ->
            event?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alertDialog = if (activity?.shouldShowRequestPermissionRationale(
                            DeviceLocationManager.permissions[0]) == false
                        || activity?.shouldShowRequestPermissionRationale(DeviceLocationManager.permissions[1]) == false) { //TODO: try to extract business logic in vm
                        activity?.showInfoDialog(
                            titleResId = R.string.grant_access_location_title,
                            messageResId = R.string.grant_access_location_description,
                            positiveButtonResId = R.string.actions_go_to_settings,
                            listener = { _, dialogActionId ->
                                viewModel.onPermissionRationaleDialogActionClick(dialogActionId, R.string.actions_go_to_settings)
                            })
                    } else {
                        activity?.showInfoDialog(
                            titleResId = R.string.grant_access_location_title,
                            messageResId = R.string.grant_access_location_description,
                            positiveButtonResId = R.string.actions_proceed,
                            listener = { _, dialogActionId ->
                                viewModel.onPermissionRationaleDialogActionClick(dialogActionId, R.string.actions_proceed)
                            })
                    }
                }
            }
        })
        viewModel.onGoToSystemSettingsEvent.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", requireActivity().packageName, null)
                startActivity(intent)
            }
        })
        viewModel.onAskPermissionsEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let {
                requestMultiplePermissions.launch(DeviceLocationManager.permissions)
            }
        })
        viewModel.onRequestGPSProviderEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                activity?.showInfoDialog(
                    titleResId = R.string.enable_gps_title,
                    messageResId = R.string.enable_gps_description,
                    positiveButtonResId = R.string.actions_enable,
                    listener = { _, dialogActionId ->
                        viewModel.onPermissionRationaleDialogActionClick(
                            dialogActionId,
                            R.string.actions_enable,
                            activity
                        )
                    })
            }
        })
    }
}
