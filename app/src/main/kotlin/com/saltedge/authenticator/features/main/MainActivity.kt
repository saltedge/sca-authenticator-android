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
package com.saltedge.authenticator.features.main

import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.databinding.MainActivityBinding
import com.saltedge.authenticator.features.actions.NewAuthorizationListener
import com.saltedge.authenticator.interfaces.*
import com.saltedge.authenticator.models.location.DeviceLocationManager
import com.saltedge.authenticator.app.authenticatorApp
import com.saltedge.authenticator.tools.currentFragmentOnTop
import com.saltedge.authenticator.tools.showQrScannerActivity
import com.saltedge.authenticator.tools.updateScreenshotLocking
import com.saltedge.authenticator.widget.security.LockableActivity
import com.saltedge.authenticator.widget.security.UnlockAppInputView
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : LockableActivity(), ViewModelContract, SnackbarAnchorContainer {

    override lateinit var viewModel: MainActivityViewModel
    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.updateScreenshotLocking()
        authenticatorApp?.appComponent?.inject(this)//inject ViewModelsFactory
        setupViewModel()
        setupBinding()
        viewModel.onLifeCycleCreate(savedInstanceState, intent)
        DeviceLocationManager.initManager(context = this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        viewModel.onNewIntent(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        DeviceLocationManager.startLocationUpdates(context = this)
    }

    override fun onPause() {
        DeviceLocationManager.stopLocationUpdates()
        super.onPause()
    }

    /**
     * Redirect back press events to fragments in container
     */
    override fun onBackPressed() {
        val onBackPressListener = currentFragmentOnTop() as? OnBackPressListener
        if (onBackPressListener?.onBackPress() != true) super.onBackPressed()
    }

    /**
     * on security lock of Activity ask current Fragment to close all active dialogs
     */
    override fun onLockActivity() {
        (currentFragmentOnTop() as? DialogHandlerListener)?.closeActiveDialogs()
    }

    override fun onUnlockActivity() {
        viewModel.onUnlock()
    }

    override fun getUnlockAppInputView(): UnlockAppInputView? = unlockAppInputView

    override fun getSnackbarAnchorView(): View? = activityRootLayout

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainActivityViewModel::class.java)
        viewModel.bindLifecycleObserver(lifecycle = lifecycle)

        viewModel.onAppbarMenuItemClickEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                (this.currentFragmentOnTop() as? AppbarMenuItemClickListener)?.onAppbarMenuItemClick(it)
            }
        })
        viewModel.onBackActionClickEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { onBackPressed() }
        })
        viewModel.onRestartActivityEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { super.restartLockableActivity() }
        })
        viewModel.onShowAuthorizationDetailsEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { bundle ->
                findNavController(R.id.navHostFragment).navigate(R.id.authorizationDetailsFragment, bundle)
            }
        })
        viewModel.onShowActionAuthorizationEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { bundle ->
                findNavController(R.id.navHostFragment).navigate(R.id.authorizationDetailsFragment, bundle)
            }
        })
        viewModel.onShowConnectEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { connectAppLinkData ->
                findNavController(R.id.navHostFragment).navigate(R.id.connectProviderFragment, connectAppLinkData)
            }
        })
        viewModel.onShowSubmitActionEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { actionAppLinkData ->
                findNavController(R.id.navHostFragment).navigate(R.id.submitActionFragment, actionAppLinkData)
            }
        })
        viewModel.onQrScanClickEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { this.showQrScannerActivity() }
        })
    }

    private fun setupBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }
}

val FragmentActivity.newAuthorizationListener: NewAuthorizationListener?
    get() = (this as? ViewModelContract)?.viewModel as? NewAuthorizationListener

val FragmentActivity.activityComponentsContract: ActivityComponentsContract?
    get() = (this as? ViewModelContract)?.viewModel as? ActivityComponentsContract
