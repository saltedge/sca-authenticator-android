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
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.authorizations.details.AuthorizationDetailsFragment
import com.saltedge.authenticator.features.authorizations.list.AuthorizationsListFragment
import com.saltedge.authenticator.features.connections.connect.ConnectProviderFragment
import com.saltedge.authenticator.features.connections.list.ConnectionsListFragment
import com.saltedge.authenticator.features.security.LockableActivity
import com.saltedge.authenticator.features.security.UnlockAppInputView
import com.saltedge.authenticator.features.settings.list.SettingsListFragment
import com.saltedge.authenticator.interfaces.ActivityComponentsContract
import com.saltedge.authenticator.interfaces.OnBackPressListener
import com.saltedge.authenticator.interfaces.UpActionImageListener
import com.saltedge.authenticator.model.db.ConnectionsRepository
import com.saltedge.authenticator.model.realm.RealmManager
import com.saltedge.authenticator.tool.*
import com.saltedge.authenticator.tool.secure.updateScreenshotLocking
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : LockableActivity(),
    MainActivityContract.View,
    ActivityComponentsContract,
    BottomNavigationView.OnNavigationItemSelectedListener,
    View.OnClickListener,
    FragmentManager.OnBackStackChangedListener {

    private val presenter = MainActivityPresenter(
        viewContract = this,
        connectionsRepository = ConnectionsRepository
    )

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
        presenter.onNavigationItemClick(isTopNavigationLevel())
    }

    override fun showConnectProvider(connectConfigurationLink: String) {
        this.addFragment(ConnectProviderFragment.newInstance(connectConfigurationLink = connectConfigurationLink))
    }

    override fun restartActivity() {
        super.restartLockableActivity()
    }

    override fun updateAppbarTitle(title: String) {
        supportActionBar?.title = title
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

    override fun showAuthorizationsList() {
        replaceFragmentInContainer(AuthorizationsListFragment())
    }

    override fun showConnectionsList() {
        replaceFragmentInContainer(ConnectionsListFragment())
    }

    override fun showSettingsList() {
        replaceFragmentInContainer(SettingsListFragment())
    }

    override fun showAuthorizationDetailsView(
        connectionId: String,
        authorizationId: String,
        quickConfirmMode: Boolean
    ) {
        this.addFragment(
            AuthorizationDetailsFragment.newInstance(
                connectionId = connectionId,
                authorizationId = authorizationId,
                quickConfirmMode = quickConfirmMode
            )
        )
    }

    override fun closeView() {
        finish()
    }

    override fun popBackStack() {
        supportFragmentManager?.popBackStack()
    }

    override fun updateNavigationViewsContent() {
        isTopNavigationLevel().also { isOnTop ->
            val imageResId = (currentFragmentInContainer() as? UpActionImageListener)?.getUpActionImage()
            toolbarView?.navigationIcon =
                (imageResId ?: presenter.getNavigationIcon(isOnTop))?.let { resId ->
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

    private fun setupViews() {
        try {
            setSupportActionBar(toolbarView)
            toolbarView?.setNavigationOnClickListener(this)
            bottomNavigationView?.setOnNavigationItemSelectedListener(this)
            supportFragmentManager?.addOnBackStackChangedListener(this)
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
