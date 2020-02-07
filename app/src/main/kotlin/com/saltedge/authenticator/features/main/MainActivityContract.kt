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

import com.saltedge.authenticator.features.connections.common.ConnectionViewModel
import com.saltedge.authenticator.sdk.model.ActionDeepLinkData
import com.saltedge.authenticator.sdk.model.GUID

interface MainActivityContract {

    interface View {
        fun showAuthorizationsList()
        fun showConnectionsList()
        fun showSettingsList()
        fun setSelectedTabbarItemId(menuId: Int)
        fun showConnectProvider(
            connectConfigurationLink: String,
            connectQuery: String?
        )
        fun showAuthorizationDetailsView(connectionID: String, authorizationID: String)

        fun restartActivity()
        fun closeView()
        fun updateNavigationViewsContent()
        fun popBackStack()
        fun showNoConnectionsError()
        fun showConnectionsSelectorFragment(connections: List<ConnectionViewModel>)
        fun showSubmitActionFragment(connectionGuid: GUID, actionDeepLinkData: ActionDeepLinkData)
    }
}
