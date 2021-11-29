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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_CLOSE_APP
import com.saltedge.authenticator.app.KEY_ID
import com.saltedge.authenticator.app.TIME_VIEW_UPDATE_TIMEOUT
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.cloud.clearNotifications
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.interfaces.OnBackPressListener
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.sdk.constants.KEY_TITLE
import com.saltedge.authenticator.sdk.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.app.authenticatorApp
import com.saltedge.authenticator.tools.popBackStack
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_authorization_details.*
import java.util.*
import javax.inject.Inject

class AuthorizationDetailsFragment : BaseFragment(),
    View.OnClickListener,
    OnBackPressListener {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: AuthorizationDetailsViewModel
    private var timeViewUpdateTimer: Timer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticatorApp?.appComponent?.inject(this)
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
        viewModel.onViewClick(view?.id ?: return)
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
        viewModel = ViewModelProvider(this, viewModelFactory).get(AuthorizationDetailsViewModel::class.java)
        viewModel.bindLifecycleObserver(lifecycle = lifecycle)

        viewModel.onTimeUpdateEvent.observe(this, Observer<ViewModelEvent<Unit>> { event ->
            event.getContentIfNotHandled()?.let {
                headerView?.onTimeUpdate()
            }
        })
        viewModel.onErrorEvent.observe(this, Observer<ViewModelEvent<String>> { event ->
            event.getContentIfNotHandled()?.let { message ->
                view?.let { anchor -> Snackbar.make(anchor, message, Snackbar.LENGTH_LONG).show() }
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
            contentView?.setViewMode(it.viewMode)
        })

        viewModel.setInitialData(
            identifier = arguments?.getSerializable(KEY_ID) as? AuthorizationIdentifier,
            closeAppOnBackPress = arguments?.getBoolean(KEY_CLOSE_APP, true),
            titleRes = arguments?.getInt(KEY_TITLE, R.string.authorization_feature_title)
        )
    }
}
