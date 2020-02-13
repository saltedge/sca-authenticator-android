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
package com.saltedge.authenticator.features.connections.create

import com.saltedge.authenticator.sdk.model.ConnectionID
import com.saltedge.authenticator.sdk.model.GUID
import com.saltedge.authenticator.sdk.model.Token
import com.saltedge.authenticator.sdk.model.appLink.ConnectAppLinkData

interface ConnectProviderContract {

    interface View {
        fun updateViewsContent()
        fun loadUrlInWebView(url: String)
        fun showErrorAndFinish(message: String)
        fun closeView()
    }

    interface Presenter {
        val logoUrl: String
        var viewContract: View?
        val iconResId: Int
        val mainActionTextResId: Int
        val reportProblemActionText: Int?
        val completeTitle: String
        val completeMessage: String
        val shouldShowProgressView: Boolean
        val shouldShowWebView: Boolean
        val shouldShowCompleteView: Boolean
        fun setInitialData(initialConnectData: ConnectAppLinkData?, connectionGuid: GUID?)
        fun onViewCreated()
        fun onDestroyView()
        fun webAuthFinishSuccess(id: ConnectionID, accessToken: Token)
        fun webAuthFinishError(errorClass: String, errorMessage: String?)
        fun onViewClick(viewId: Int)
        fun getTitleResId(): Int
    }
}
