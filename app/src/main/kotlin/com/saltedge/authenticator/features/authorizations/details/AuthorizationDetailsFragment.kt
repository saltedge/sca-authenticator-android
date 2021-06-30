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
package com.saltedge.authenticator.features.authorizations.details

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.fragment_authorization_details.*
import java.util.*
import javax.inject.Inject

class AuthorizationDetailsFragment : BaseFragment(),
    View.OnClickListener,
    OnBackPressListener,
    DialogHandlerListener {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: AuthorizationDetailsViewModel
    private var timeViewUpdateTimer: Timer = Timer()
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityComponents?.updateAppbar(
            titleResId = viewModel.titleRes,
            backActionImageResId = R.drawable.ic_appbar_action_close
        )
        return inflater.inflate(R.layout.fragment_authorization_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        contentView?.setActionClickListener(this)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.onRequestPermissionsResult(requestCode, grantResults)
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
                headerView?.onTimeUpdate()
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
            headerView?.setTitleAndLogo(title = it.connectionName, logoUrl = it.connectionLogoUrl ?: "")
            headerView?.setProgressTime(startTime = it.startTime, endTime = it.endTime)
            headerView?.ignoreTimeUpdate = it.ignoreTimeUpdate
            headerView?.visibility = it.timeViewVisibility
            contentView?.setTitleAndDescription(it.title, it.description)
            contentView?.setViewMode(it.status)
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
                requestPermissions(DeviceLocationManager.permissions, LOCATION_PERMISSION_REQUEST_CODE)
            }
        })
        viewModel.onRequestGPSProviderEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                activity?.showInfoDialog(
                    titleResId = R.string.enable_gps_title,
                    messageResId = R.string.enable_gps_description,
                    positiveButtonResId = R.string.actions_enable,
                    listener = { _, dialogActionId ->
                        viewModel.onPermissionRationaleDialogActionClick(dialogActionId, R.string.actions_enable)
                    })
            }
        })
        viewModel.onEnableGpsEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        })
    }
}
