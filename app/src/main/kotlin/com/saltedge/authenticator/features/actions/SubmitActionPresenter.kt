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
package com.saltedge.authenticator.features.actions

import android.content.Context
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.authorizations.common.createConnectionAndKey
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.contract.ActionSubmitListener
import com.saltedge.authenticator.sdk.model.*
import com.saltedge.authenticator.sdk.model.response.SubmitActionData
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import javax.inject.Inject

class SubmitActionPresenter @Inject constructor(
    private val appContext: Context,
    private val keyStoreManager: KeyStoreManagerAbs,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val apiManager: AuthenticatorApiManagerAbs
) : SubmitActionContract.Presenter, ActionSubmitListener {

    override var viewContract: SubmitActionContract.View? = null
    private var viewMode: ViewMode = ViewMode.START
    private var actionDeepLinkData: ActionDeepLinkData? = null
    private var connectionAndKey: ConnectionAndKey? = null

    override fun setInitialData(connectionGuid: GUID, actionDeepLinkData: ActionDeepLinkData) {
        this.connectionAndKey = connectionsRepository.getByGuid(connectionGuid)?.let {
            this.actionDeepLinkData = actionDeepLinkData
            createConnectionAndKey(
                connection = it,
                keyStoreManager = keyStoreManager
            )
        }
        if (connectionAndKey == null) viewMode = ViewMode.ACTION_ERROR
    }

    override fun getTitleResId(): Int = R.string.action_authentication

    override fun onViewCreated() {
        if (viewMode == ViewMode.START) {
            apiManager.sendAction(
                actionUUID = actionDeepLinkData?.actionUuid ?: "",
                connectionAndKey = connectionAndKey ?: return,
                resultCallback = this
            )
            viewMode = ViewMode.PROCESSING
        }
        setupViews()
    }

    override fun onActionInitFailure(error: ApiErrorData) {
        viewMode = ViewMode.ACTION_ERROR
        setupViews()
        viewContract?.showErrorAndFinish(error.getErrorMessage(appContext))
    }

    override fun onActionInitSuccess(response: SubmitActionData) {
        val authorizationID = response.authorizationId ?: ""
        val connectionID = response.connectionId ?: ""
        if (response.success == true && authorizationID.isEmpty() && connectionID.isEmpty()) {
            viewMode = ViewMode.ACTION_SUCCESS
            setupViews()
        } else {
            viewMode = ViewMode.ACTION_SUCCESS
            viewContract?.closeView()
            viewContract?.setResultAuthorizationIdentifier(
                authorizationIdentifier = AuthorizationIdentifier(
                    authorizationID = authorizationID,
                    connectionID = connectionID
                )
            )
        }
    }

    override fun onViewClick(viewId: Int) {
        viewContract?.closeView()
        //TODO: Open return to in browser also when confirm/deny in AuthorizationDetailsFragment
    }

    private fun setupViews() {
        when (viewMode) {
            ViewMode.ACTION_SUCCESS -> {
                viewContract?.setProcessingVisibility(false)
                viewContract?.updateCompleteViewContent(
                    iconResId = R.drawable.ic_complete_ok_70,
                    completeTitleResId = R.string.action_feature_title,
                    completeMessageResId = R.string.action_feature_description,
                    mainActionTextResId = R.string.actions_proceed
                )
            }
            ViewMode.ACTION_ERROR -> {
                viewContract?.setProcessingVisibility(false)
                viewContract?.updateCompleteViewContent(
                    iconResId = R.drawable.ic_auth_error_70,
                    completeTitleResId = R.string.action_error_title,
                    completeMessageResId = R.string.action_error_description,
                    mainActionTextResId = R.string.actions_try_again
                )
            }
            ViewMode.PROCESSING -> viewContract?.setProcessingVisibility(true)
            ViewMode.START -> Unit
        }
    }
}

private enum class ViewMode {
    START, PROCESSING, ACTION_SUCCESS, ACTION_ERROR
}
