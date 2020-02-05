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
package com.saltedge.authenticator.features.connections.actions

import android.content.Context
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.authorizations.common.createConnectionAndKey
import com.saltedge.authenticator.model.db.Connection
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.contract.ActionInitResult
import com.saltedge.authenticator.sdk.model.ActionDeepLinkData
import com.saltedge.authenticator.sdk.model.ApiErrorData
import com.saltedge.authenticator.sdk.model.GUID
import com.saltedge.authenticator.sdk.model.getErrorMessage
import com.saltedge.authenticator.sdk.model.response.ActionData
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import javax.inject.Inject

class ActionPresenter @Inject constructor(
    private val appContext: Context,
    private val keyStoreManager: KeyStoreManagerAbs,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val apiManager: AuthenticatorApiManagerAbs
) : ActionContract.Presenter, ActionInitResult {

    override var viewContract: ActionContract.View? = null
    override val iconResId: Int
        get() = if (showActionSuccess()) R.drawable.ic_complete_ok_70 else R.drawable.ic_auth_error_70
    override val completeTitle: String
        get() = if (showActionSuccess()) appContext.getString(R.string.action_feature_title)
        else appContext.getString(R.string.action_error_title)
    override val completeMessage: String
        get() = if (showActionSuccess()) appContext.getString(R.string.action_feature_description)
        else appContext.getString(R.string.action_error_description)
    override val mainActionTextResId: Int
        get() = if (showActionSuccess()) R.string.actions_proceed else R.string.actions_try_again
    override var showCompleteView: Boolean = false
    private var actionDeepLinkData: ActionDeepLinkData? = null
    private var connection = Connection()
    private var showActionError = false

    override fun setInitialData(connectionGuid: GUID, actionDeepLinkData: ActionDeepLinkData) {
        this.connection = connectionsRepository.getByGuid(connectionGuid) ?: Connection()
        this.actionDeepLinkData = actionDeepLinkData
    }

    override fun onDestroyView() {
        if (connection.guid.isNotEmpty() && connection.accessToken.isEmpty()) {
            keyStoreManager.deleteKeyPair(connection.guid)
        }
    }

    override fun getTitleResId(): Int = R.string.connections_new_connection

    override fun onViewCreated() {
        val connectionAndKey = createConnectionAndKey(
            connection = this.connection,
            keyStoreManager = keyStoreManager
        )
        apiManager.sendAction(
            actionUUID = actionDeepLinkData?.actionUuid ?: "",
            connectionAndKey = connectionAndKey ?: return,
            resultCallback = this
        )
    }

    override fun onActionInitFailure(error: ApiErrorData) {
        showCompleteView = true
        showActionError = true
        viewContract?.showErrorAndFinish(error.getErrorMessage(appContext))
    }

    override fun onActionInitSuccess(response: ActionData) {
        val authorizationID = response.authorizationId ?: ""
        val connectionID = response.connectionId ?: ""
        if (response.success == true && authorizationID.isEmpty() && connectionID.isEmpty()) {
            showCompleteView = true
            viewContract?.updateViewsContent()
        } else {
            viewContract?.returnActionWithConnectionId(
                authorizationID = authorizationID,
                connectionID = connectionID
            )
        }
    }

    override fun onViewClick(viewId: Int) {
        if (viewId == R.id.mainActionView) {
            viewContract?.closeView()
        }
    }

    private fun showActionSuccess(): Boolean {
        return !showActionError
    }
}
