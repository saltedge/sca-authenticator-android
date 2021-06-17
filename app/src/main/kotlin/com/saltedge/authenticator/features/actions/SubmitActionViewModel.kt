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
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.R
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ActionAppLinkData
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.connections.list.convertConnectionsToViewModels
import com.saltedge.authenticator.features.connections.select.SelectConnectionsFragment.Companion.dataBundle
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.toRichConnection
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.sdk.api.model.response.SubmitActionResponseData
import com.saltedge.authenticator.sdk.contract.ActionSubmitListener
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import com.saltedge.authenticator.sdk.v2.api.contract.AuthorizationCreateListener
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.getErrorMessage
import com.saltedge.authenticator.tools.postUnitEvent
import timber.log.Timber

class SubmitActionViewModel(
    private val appContext: Context,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyManagerAbs,
    private val apiManagerV1: AuthenticatorApiManagerAbs,
    private val apiManagerV2: ScaServiceClientAbs,
    private val locationManager: DeviceLocationManagerAbs
) : ViewModel(), LifecycleObserver, ActionSubmitListener, AuthorizationCreateListener {

    private var viewMode: ViewMode = ViewMode.START
    private var actionAppLinkData: ActionAppLinkData? = null
    private var richConnection: RichConnection? = null
    val onCloseEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onOpenLinkEvent = MutableLiveData<ViewModelEvent<Uri>>()
    val showConnectionsSelectorFragmentEvent = MutableLiveData<ViewModelEvent<Bundle>>()
    val setResultAuthorizationIdentifier = MutableLiveData<AuthorizationIdentifier>()
    val iconResId: MutableLiveData<Int> = MutableLiveData(R.drawable.ic_status_error)
    val completeTitleResId: MutableLiveData<Int> = MutableLiveData(R.string.action_error_title)
    val completeDescription: MutableLiveData<String> = MutableLiveData("")
    val mainActionTextResId: MutableLiveData<Int> = MutableLiveData(R.string.actions_try_again)

    var completeViewVisibility = MutableLiveData<Int>(View.GONE)
    var actionProcessingVisibility = MutableLiveData<Int>(View.VISIBLE)

    fun setInitialData(actionAppLinkData: ActionAppLinkData) {
        val connections = collectConnections(actionAppLinkData)
        this.actionAppLinkData = actionAppLinkData
        when {
            connections.isEmpty() -> showActionError(R.string.errors_actions_no_connections_link_app)
            connections.size == 1 -> {
                this.richConnection = connections.firstOrNull()?.toRichConnection(keyStoreManager)
                if (richConnection == null) viewMode = ViewMode.ACTION_ERROR
            }
            else -> showConnectionsSelector(connections)
        }
    }

    fun onViewCreated() {
        val currentRichConnection = richConnection
        if (viewMode == ViewMode.START && currentRichConnection != null) {
            sendActionRequest(currentRichConnection, actionAppLinkData?.actionIdentifier ?: "")
            viewMode = ViewMode.PROCESSING
        }
        updateViewsContent()
    }

    private fun sendActionRequest(currentRichConnection: RichConnection, actionID: ID) {
        if (currentRichConnection.connection.apiVersion == API_V2_VERSION) {
            apiManagerV2.requestCreateAuthorizationForAction(
                richConnection = currentRichConnection,
                actionID = actionID,
                callback = this
            )
        } else {
            apiManagerV1.sendAction(
                connectionAndKey = currentRichConnection,
                actionUUID = actionID,
                resultCallback = this
            )
        }
    }

    fun onViewClick(viewId: Int) {
        if (viewId == R.id.actionView) {
            onCloseEvent.postUnitEvent()
            openReturnToUrl()
        }
    }

    fun onConnectionSelected(guid: GUID) {
        if (guid.isEmpty()) {
            onCloseEvent.postUnitEvent()
        } else {
            this.richConnection = connectionsRepository.getByGuid(guid)?.toRichConnection(keyStoreManager)
            viewMode = if (richConnection == null) ViewMode.ACTION_ERROR else ViewMode.START
            onViewCreated()
        }
    }

    override fun onActionInitSuccess(response: SubmitActionResponseData) {
        openNewAuthorization(response.connectionId ?: "", response.authorizationId ?: "")
    }

    override fun onActionInitFailure(error: ApiErrorData) {
        showActionError(error.getErrorMessage(appContext))
    }

    override fun onAuthorizationCreateSuccess(connectionID: ID, authorizationID: ID) {
        openNewAuthorization(connectionID, authorizationID)
    }

    override fun onAuthorizationCreateFailure(error: ApiErrorData) {
        showActionError(error.getErrorMessage(appContext))
    }

    private fun updateViewsContent() {
        mainActionTextResId.postValue(R.string.actions_done)
        val showError = viewMode == ViewMode.ACTION_ERROR
        if (showError) {
            iconResId.postValue(R.drawable.ic_status_error)
            completeTitleResId.postValue(R.string.action_error_title)
        }
        completeViewVisibility.postValue(if (showError) View.VISIBLE else View.GONE)
        actionProcessingVisibility.postValue(if (showError) View.GONE else View.VISIBLE)
    }

    private fun showActionError(errorMessageRes: ResId) {
        showActionError(appContext.getString(errorMessageRes))
    }

    private fun showActionError(errorMessage: String) {
        viewMode = ViewMode.ACTION_ERROR
        updateViewsContent()
        completeDescription.postValue(
            if (errorMessage.isEmpty()) appContext.getString(R.string.action_error_description) else errorMessage
        )
    }

    private fun showConnectionsSelector(connections: List<Connection>) {
        val result = connections.convertConnectionsToViewModels(
            context = appContext,
            locationManager = locationManager
        )
        viewMode = ViewMode.SELECT
        showConnectionsSelectorFragmentEvent.postValue(ViewModelEvent(dataBundle(result)))
    }

    private fun openReturnToUrl() {
        val returnTo = actionAppLinkData?.returnTo ?: return
        if (returnTo.isNotEmpty()) {
            try {
                onOpenLinkEvent.postValue(ViewModelEvent(Uri.parse(returnTo)))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun openNewAuthorization(connectionID: String, authorizationID: String) {
        if (authorizationID.isNotEmpty() && connectionID.isNotEmpty()) {
            setResultAuthorizationIdentifier.postValue(
                AuthorizationIdentifier(
                    authorizationID = authorizationID,
                    connectionID = connectionID
                )
            )
        } else showActionError(R.string.errors_actions_not_success)
    }

    private fun collectConnections(actionAppLinkData: ActionAppLinkData): List<Connection> {
        val connections = if (actionAppLinkData.apiVersion == API_V2_VERSION) {
            actionAppLinkData.providerID?.let {
                connectionsRepository.getAllActiveByProvider(providerID = it)
            }
        } else {
            actionAppLinkData.connectUrl?.let {
                connectionsRepository.getAllActiveByConnectUrl(connectionUrl = it)
            }
        }
        return connections ?: emptyList()
    }
}

enum class ViewMode {
    START, PROCESSING, ACTION_ERROR, SELECT
}
