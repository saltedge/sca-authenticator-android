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

import android.app.Activity
import android.content.Intent
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_DEEP_LINK
import com.saltedge.authenticator.app.QR_SCAN_REQUEST_CODE
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.constants.KEY_AUTHORIZATION_ID
import com.saltedge.authenticator.sdk.constants.KEY_CONNECTION_ID
import com.saltedge.authenticator.sdk.tools.extractConnectConfigurationLink
import com.saltedge.authenticator.sdk.tools.extractConnectQuery
import com.saltedge.authenticator.tool.ResId

class MainActivityPresenter(
    val viewContract: MainActivityContract.View,
    private val connectionsRepository: ConnectionsRepositoryAbs
) {

    /**
     * Starts initial fragment
     * if activity is in quickConfirmMode then shows AuthorizationDetails
     * if exist active connection in db then shows AuthorizationsList
     * else shows ConnectionsList
     */
    fun launchInitialFragment(intent: Intent?) {
        if (intent != null && (intent.hasConnectionIdAndAuthorizationId || intent.hasDeepLink)) {
            onNewIntentReceived(intent)
        } else {
            viewContract.setSelectedTabbarItemId(
                if (connectionsRepository.hasActiveConnections()) {
                    R.id.menu_authorizations
                } else {
                    R.id.menu_connections
                }
            )
        }
    }

    /**
     * On new intent with data received
     */
    fun onNewIntentReceived(intent: Intent) {
        when {
            intent.hasConnectionIdAndAuthorizationId -> {
                viewContract.showAuthorizationDetailsView(
                    connectionID = intent.connectionId,
                    authorizationID = intent.authorizationId
                )
            }
            intent.hasDeepLink -> {
                viewContract.setSelectedTabbarItemId(R.id.menu_connections)
                intent.deepLink.extractConnectConfigurationLink()?.let {
                    viewContract.showConnectProvider(
                        connectConfigurationLink = it,
                        connectQuery = intent.deepLink.extractConnectQuery()
                    )
                }
            }
        }
    }

    /**
     * Returns navigation icon resource
     *
     * @param isTopNavigationLevel
     * @return icon resource id
     */
    fun getNavigationIcon(isTopNavigationLevel: Boolean): ResId? =
        if (isTopNavigationLevel) null else R.drawable.ic_arrow_back_white_24dp

    /**
     * Handles navigation (Tab Bar) items clicks
     *
     * @param itemId - id of clicked tab item
     * @return - true if event is captured
     */
    fun onNavigationItemSelected(itemId: Int): Boolean {
        when (itemId) {
            R.id.menu_authorizations -> viewContract.showAuthorizationsList()
            R.id.menu_connections -> viewContract.showConnectionsList()
            R.id.menu_settings -> viewContract.showSettingsList()
            else -> return false
        }
        return true
    }

    /**
     * Handles result from QR Scan Activity
     * if result is correct then shows Connect Provider Fragment
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == QR_SCAN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let { onNewIntentReceived(intent = it) }
        }
    }

    /**
     * Handles back stack changes
     * if user go back from in quickConfirmMode then will be closed activity
     * else will be updated navigation components
     *
     * @param stackIsClear Boolean state of fragments stack
     */
    fun onFragmentBackStackChanged(stackIsClear: Boolean, intent: Intent?) {
        if (intent?.hasConnectionIdAndAuthorizationId == true && stackIsClear) viewContract.closeView()
        else viewContract.updateNavigationViewsContent()
    }

    /**
     * Handles clicks on navigation action (back arrow)
     *
     * @param stackIsClear Boolean state of fragments stack
     */
    fun onNavigationItemClick(stackIsClear: Boolean) {
        if (stackIsClear) viewContract.closeView() else viewContract.popBackStack()
    }

    private val Intent?.connectionId: String
        get() = this?.getStringExtra(KEY_CONNECTION_ID) ?: ""

    private val Intent?.authorizationId: String
        get() = this?.getStringExtra(KEY_AUTHORIZATION_ID) ?: ""

    private val Intent?.deepLink: String
        get() = this?.getStringExtra(KEY_DEEP_LINK) ?: ""

    // Show Authorization Details Fragment
    private val Intent?.hasConnectionIdAndAuthorizationId: Boolean
        get() = this != null && this.connectionId.isNotEmpty() && this.authorizationId.isNotEmpty()

    // Show Connect Activity
    private val Intent?.hasDeepLink: Boolean
        get() = deepLink.isNotEmpty()
}
