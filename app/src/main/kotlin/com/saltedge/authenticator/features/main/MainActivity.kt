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
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.databinding.MainActivityBinding
import com.saltedge.authenticator.features.actions.NewAuthorizationListener
import com.saltedge.authenticator.features.actions.SubmitActionFragment
import com.saltedge.authenticator.features.authorizations.details.AuthorizationDetailsFragment
import com.saltedge.authenticator.features.authorizations.list.AuthorizationsListFragment
import com.saltedge.authenticator.features.connections.create.ConnectProviderFragment
import com.saltedge.authenticator.features.connections.list.ConnectionsListFragment
import com.saltedge.authenticator.features.menu.BottomMenuDialog
import com.saltedge.authenticator.features.menu.MenuItemSelectListener
import com.saltedge.authenticator.features.settings.list.SettingsListFragment
import com.saltedge.authenticator.interfaces.ActivityComponentsContract
import com.saltedge.authenticator.interfaces.DialogHandlerListener
import com.saltedge.authenticator.interfaces.OnBackPressListener
import com.saltedge.authenticator.interfaces.ViewModelContract
import com.saltedge.authenticator.tools.*
import com.saltedge.authenticator.widget.security.LockableActivity
import com.saltedge.authenticator.widget.security.UnlockAppInputView
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : LockableActivity(),
    ViewModelContract,
    SnackbarAnchorContainer,
    MenuItemSelectListener
{
    override lateinit var viewModel: MainActivityViewModel
    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var binding: MainActivityBinding
    private var dialogFragment: DialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.updateScreenshotLocking()
        authenticatorApp?.appComponent?.inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupViewModel()
        viewModel.onLifeCycleCreate(savedInstanceState, intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        viewModel.onNewIntent(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Redirect back press events to fragments in container
     */
    override fun onBackPressed() {
        val onBackPressListener = currentFragmentInContainer() as? OnBackPressListener
        if (onBackPressListener?.onBackPress() != true) super.onBackPressed()
    }

    /**
     * on security lock of Activity ask current Fragment to close all active dialogs
     */
    override fun onLockActivity() {
        (currentFragmentInContainer() as? DialogHandlerListener)?.closeActiveDialogs()
        if (dialogFragment?.isVisible == true) dialogFragment?.dismiss()
    }

    override fun onUnlockActivity() {
        viewModel.onUnlock()
    }

    override fun getUnlockAppInputView(): UnlockAppInputView? = unlockAppInputView

    override fun getSnackbarAnchorView(): View? = container

    override fun onMenuItemSelected(menuId: String, selectedItemId: Int) {
        viewModel.onMenuItemSelected(menuId, selectedItemId)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainActivityViewModel::class.java)
        viewModel.bindLifecycleObserver(lifecycle = lifecycle)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this

        viewModel.onQrScanClickEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { this.showQrScannerActivity() }
        })
        viewModel.onAppBarMenuClickEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { menuItems ->
                dialogFragment = BottomMenuDialog.newInstance(menuItems = menuItems).also {
                    this.showDialogFragment(it)
                }
            }
        })
        viewModel.onBackActionClickEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { onBackPressed() }
        })
        viewModel.onRestartActivityEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { super.restartLockableActivity() }
        })
        viewModel.onShowAuthorizationsListEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                this.replaceFragmentInContainer(AuthorizationsListFragment())
            }
        })
        viewModel.onShowAuthorizationDetailsEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { authorizationIdentifier ->
                this.addFragment(
                    fragment = AuthorizationDetailsFragment.newInstance(
                        identifier = authorizationIdentifier,
                        closeAppOnBackPress = true
                    ),
                    animateTransition = true
                )
            }
        })
        viewModel.onShowActionAuthorizationEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { authorizationIdentifier ->
                this.addFragment(
                    fragment = AuthorizationDetailsFragment.newInstance(
                        identifier = authorizationIdentifier,
                        closeAppOnBackPress = true,
                        titleRes = R.string.action_new_action_title
                    ),
                    animateTransition = false
                )
            }
        })
        viewModel.onShowConnectionsListEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { this.addFragment(ConnectionsListFragment()) }
        })
        viewModel.onShowSettingsListEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { this.addFragment(SettingsListFragment()) }
        })
        viewModel.onShowConnectEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { connectAppLinkData ->
                this.addFragment(
                    fragment = ConnectProviderFragment.newInstance(connectAppLinkData = connectAppLinkData),
                    animateTransition = false
                )
            }
        })
        viewModel.onShowSubmitActionEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { actionAppLinkData ->
                this.addFragment(
                    fragment = SubmitActionFragment.newInstance(actionAppLinkData = actionAppLinkData),
                    animateTransition = false
                )
            }
        })
    }
}

val FragmentActivity.newAuthorizationListener: NewAuthorizationListener?
    get() = (this as? ViewModelContract)?.viewModel as? NewAuthorizationListener

val FragmentActivity.activityComponentsContract: ActivityComponentsContract?
    get() = (this as? ViewModelContract)?.viewModel as? ActivityComponentsContract
