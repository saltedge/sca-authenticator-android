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
import android.util.Log
import com.saltedge.authenticator.R
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.contract.ActionSubmitListener
import com.saltedge.authenticator.sdk.model.appLink.ActionAppLinkData
import com.saltedge.authenticator.sdk.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.model.error.getErrorMessage
import com.saltedge.authenticator.sdk.model.response.SubmitActionResponseData
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
    private var actionAppLinkData: ActionAppLinkData? = null
    private var connectionAndKey: ConnectionAndKey? = null

    override fun setInitialData(actionAppLinkData: ActionAppLinkData) {
        val connections = connectionsRepository.getByConnectUrl(actionAppLinkData.connectUrl)
        val connectionGuid = connections.first().guid
        this.connectionAndKey = connectionsRepository.getByGuid(connectionGuid)?.let {
            this.actionAppLinkData = actionAppLinkData
            keyStoreManager.createConnectionAndKeyModel(it)
        }
        if (connectionAndKey == null) {
            Log.d("some", "connectionAndKey == null")
            viewMode = ViewMode.ACTION_ERROR
        }

                when {
                    connections.isEmpty() -> {
                        Log.d("some", "connections is empty")
//                        viewContract.showNoConnectionsError()
                    }
                    connections.size == 1 -> {
                        Log.d("some", "connections.size 1")

                        //                        val connectionGuid = connections.first().guid
//                        viewContract.showSubmitActionFragment(
//                            connectionGuid = connectionGuid,
//                            actionAppLinkData = actionAppLinkData
//                        )
                    }
                    else -> {
                        Log.d("some", "connections.size more than 1")
//                        val result = connections.convertConnectionsToViewModels(
//                            context = appContext
//                        )
//                        viewContract.showConnectionsSelectorFragment(result)
                    }
                }

        Log.d("some", "connectionGuid: $connectionGuid , actionAppLinkData: $actionAppLinkData")
    }

    override fun onViewCreated() {
        if (viewMode == ViewMode.START) {
            apiManager.sendAction(
                actionUUID = actionAppLinkData?.actionUUID ?: "",
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

    override fun onActionInitSuccess(response: SubmitActionResponseData) {
        val authorizationID = response.authorizationId ?: ""
        val connectionID = response.connectionId ?: ""
        if (response.success == true && authorizationID.isNotEmpty() && connectionID.isNotEmpty()) {
            viewMode = ViewMode.ACTION_SUCCESS
            viewContract?.closeView()
            viewContract?.setResultAuthorizationIdentifier(
                authorizationIdentifier = AuthorizationIdentifier(
                    authorizationID = authorizationID,
                    connectionID = connectionID
                )
            )
        } else {
            viewMode = if (response.success == true) ViewMode.ACTION_SUCCESS else ViewMode.ACTION_ERROR
            setupViews()
        }
    }

    override fun onViewClick(viewId: Int) {
        viewContract?.closeView()
        val returnToUrl: String? = actionAppLinkData?.returnTo
        if (returnToUrl.isNullOrEmpty()) return
        viewContract?.openLink(returnToUrl)
    }

    private fun setupViews() {
        when (viewMode) {
            ViewMode.ACTION_SUCCESS -> {
                viewContract?.setProcessingVisibility(false)
                viewContract?.updateCompleteViewContent(
                    iconResId = R.drawable.ic_status_success,
                    completeTitleResId = R.string.action_feature_title,
                    completeMessageResId = R.string.action_feature_description,
                    mainActionTextResId = android.R.string.ok
                )
            }
            ViewMode.ACTION_ERROR -> {
                viewContract?.setProcessingVisibility(false)
                viewContract?.updateCompleteViewContent(
                    iconResId = R.drawable.ic_status_error,
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

//enum class ViewMode {
//    START, PROCESSING, ACTION_SUCCESS, ACTION_ERROR
//}
