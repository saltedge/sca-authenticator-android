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
package com.saltedge.authenticator.features.main

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.authorizations.details.AuthorizationDetailsFragment
import com.saltedge.authenticator.features.authorizations.list.AuthorizationsListFragment
import com.saltedge.authenticator.features.actions.AuthorizationListener
import com.saltedge.authenticator.features.actions.SubmitActionFragment
import com.saltedge.authenticator.features.connections.common.ConnectionViewModel
import com.saltedge.authenticator.features.connections.create.ConnectProviderFragment
import com.saltedge.authenticator.features.connections.list.ConnectionsListFragment
import com.saltedge.authenticator.features.connections.select.ConnectionSelectorListener
import com.saltedge.authenticator.features.connections.select.SelectConnectionsFragment
import com.saltedge.authenticator.features.security.LockableActivity
import com.saltedge.authenticator.features.security.UnlockAppInputView
import com.saltedge.authenticator.features.settings.list.SettingsListFragment
import com.saltedge.authenticator.interfaces.ActivityComponentsContract
import com.saltedge.authenticator.interfaces.OnBackPressListener
import com.saltedge.authenticator.interfaces.UpActionImageListener
import com.saltedge.authenticator.model.db.ConnectionsRepository
import com.saltedge.authenticator.model.realm.RealmManager
import com.saltedge.authenticator.sdk.model.appLink.ActionAppLinkData
import com.saltedge.authenticator.sdk.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.sdk.model.GUID
import com.saltedge.authenticator.sdk.model.appLink.ConnectAppLinkData
import com.saltedge.authenticator.tool.*
import com.saltedge.authenticator.tool.secure.updateScreenshotLocking
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : LockableActivity(),
    MainActivityContract.View,
    ActivityComponentsContract,
    BottomNavigationView.OnNavigationItemSelectedListener,
    View.OnClickListener,
    FragmentManager.OnBackStackChangedListener,
    NetworkStateChangeListener,
    SnackbarAnchorContainer,
    ConnectionSelectorListener,
    AuthorizationListener {

    private val presenter = MainActivityPresenter(
        viewContract = this,
        connectionsRepository = ConnectionsRepository,
        appContext = this
    )
    private val connectivityReceiver = ConnectivityReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!RealmManager.initialized) RealmManager.initRealm(context = this)
        super.onCreate(savedInstanceState)
        this.updateScreenshotLocking()
        setContentView(R.layout.activity_main)
        setupViews()
        if (savedInstanceState == null) {
            presenter.launchInitialFragment(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { presenter.onNewIntentReceived(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        this.applyPreferenceLocale()
        registerReceiver(
            connectivityReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
        connectivityReceiver.networkStateListener = this
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(connectivityReceiver)
        connectivityReceiver.networkStateListener = null
    }

    override fun onBackPressed() {
        val onBackPressListener = currentFragmentInContainer() as? OnBackPressListener
        if (onBackPressListener?.onBackPress() != true) {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return presenter.onNavigationItemSelected(item.itemId)
    }

    override fun onClick(v: View?) {
        when (v?.id ?: return) {
            R.id.actionButton -> this.startQrScannerActivity()
            else -> presenter.onNavigationItemClick(isTopNavigationLevel())
        }
    }

    override fun restartActivity() {
        super.restartLockableActivity()
    }

    override fun updateAppbarTitleWithFabAction(title: String, action: FabState) {
        supportActionBar?.title = title
        if (action === FabState.NO_ACTION) actionButton?.hide() else actionButton?.show()
    }

    override fun showActionBar() {
        supportActionBar?.show()
    }

    override fun hideActionBar() {
        supportActionBar?.hide()
    }

    override fun hideNavigationBar() {
        bottomNavigationView?.setVisible(show = false)
    }

    override fun showNavigationBar() {
        bottomNavigationView?.setVisible(show = true)
    }

    override fun setSelectedTabbarItemId(menuId: Int) {
        bottomNavigationView?.selectedItemId = menuId
    }

    override fun showConnectProvider(connectAppLinkData: ConnectAppLinkData) {
        this.addFragment(ConnectProviderFragment.newInstance(connectAppLinkData = connectAppLinkData))
    }

    override fun showAuthorizationsList() {
        replaceFragmentInContainer(AuthorizationsListFragment())
    }

    override fun showConnectionsList() {
        replaceFragmentInContainer(ConnectionsListFragment())
    }

    override fun showSettingsList() {
        replaceFragmentInContainer(SettingsListFragment())
    }

    override fun showAuthorizationDetailsView(connectionID: String, authorizationID: String) {
        this.addFragment(
            AuthorizationDetailsFragment.newInstance(
                connectionId = connectionID,
                authorizationId = authorizationID
            )
        )
    }

    override fun onNewAuthorization(authorizationIdentifier: AuthorizationIdentifier) {
        this.addFragment(
            AuthorizationDetailsFragment.newInstance(
                connectionId = authorizationIdentifier.connectionID,
                authorizationId = authorizationIdentifier.authorizationID
            )
        )
    }

    override fun closeView() {
        finish()
    }

    override fun popBackStack() {
        supportFragmentManager.popBackStack()
    }

    override fun showNoConnectionsError() {
        val snackbar = this.buildWarning(
            text = getString(R.string.connections_list_no_connections),
            snackBarDuration = 5000
        )
        snackbar?.show()
    }

    override fun showSubmitActionFragment(
        connectionGuid: GUID,
        actionAppLinkData: ActionAppLinkData
    ) {
        this.addFragment(
            SubmitActionFragment.newInstance(
                connectionGuid = connectionGuid,
                actionAppLinkData = actionAppLinkData
            )
        )
    }

    override fun showConnectionsSelectorFragment(connections: List<ConnectionViewModel>) {
        this.addFragment(SelectConnectionsFragment.newInstance(connections = connections))
    }

    override fun updateNavigationViewsContent() {
        isTopNavigationLevel().also { isOnTop ->
            toolbarView?.navigationIcon =
                ((currentFragmentInContainer() as? UpActionImageListener)?.getUpActionImageResId()
                    ?: presenter.getNavigationIcon(isOnTop))?.let { resId ->
                    this.getDrawable(resId)
                }

            bottomNavigationLayout?.setVisible(show = isOnTop)
        }
    }

    override fun getUnlockAppInputView(): UnlockAppInputView? = unlockAppInputView

    override fun getAppBarLayout(): View? = appBarLayout

    override fun onBackStackChanged() {
        presenter.onFragmentBackStackChanged(isTopNavigationLevel(), intent)
    }

    override fun getSnackbarAnchorView(): View? = snackBarCoordinator

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showNetworkMessage(isConnected)
    }

    override fun onConnectionSelected(connectionGuid: String) {
        presenter.onConnectionSelected(connectionGuid)
    }

    private fun showNetworkMessage(isConnected: Boolean) {
        if (isConnected) {
            snackbar?.dismiss()
        } else {
            snackbar = this.buildWarning(getString(R.string.warning_no_internet_connection))
            snackbar?.show()
        }
    }

    private fun setupViews() {
        try {
            setSupportActionBar(toolbarView)
            toolbarView?.setNavigationOnClickListener(this)
            bottomNavigationView?.setOnNavigationItemSelectedListener(this)
            supportFragmentManager.addOnBackStackChangedListener(this)
            actionButton?.setOnClickListener(this)
            updateNavigationViewsContent()
        } catch (e: Exception) {
            e.log()
        }
    }

    private fun replaceFragmentInContainer(fragment: BaseFragment) {
        if (currentFragmentInContainer()?.javaClass != fragment.javaClass) {
            this.replaceFragment(fragment)
        }
    }
}
