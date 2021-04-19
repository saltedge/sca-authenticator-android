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
import com.saltedge.authenticator.features.connections.list.convertConnectionsToViewModels
import com.saltedge.authenticator.features.connections.select.SelectConnectionsFragment.Companion.dataBundle
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.contract.ActionSubmitListener
import com.saltedge.authenticator.sdk.api.model.GUID
import com.saltedge.authenticator.sdk.api.model.appLink.ActionAppLinkData
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.sdk.api.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.api.model.error.getErrorMessage
import com.saltedge.authenticator.sdk.api.model.response.SubmitActionResponseData
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.tools.postUnitEvent
import timber.log.Timber

class SubmitActionViewModel(
    private val appContext: Context,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyStoreManagerAbs,
    private val apiManager: AuthenticatorApiManagerAbs
) : ViewModel(), LifecycleObserver, ActionSubmitListener {

    private var viewMode: ViewMode = ViewMode.START
    private var actionAppLinkData: ActionAppLinkData? = null
    private var connectionAndKey: ConnectionAndKey? = null
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

    override fun onActionInitFailure(error: ApiErrorData) {
        showActionError(error.getErrorMessage(appContext))
    }

    override fun onActionInitSuccess(response: SubmitActionResponseData) {
        val authorizationID = response.authorizationId ?: ""
        val connectionID = response.connectionId ?: ""
        if (response.success == true && authorizationID.isNotEmpty() && connectionID.isNotEmpty()) {
            setResultAuthorizationIdentifier.postValue(
                AuthorizationIdentifier(
                    authorizationID = authorizationID,
                    connectionID = connectionID
                )
            )
        } else {
            showActionError(appContext.getString(R.string.errors_actions_not_success))
        }
    }

    fun setInitialData(actionAppLinkData: ActionAppLinkData) {
        val connections = connectionsRepository.getByConnectUrl(actionAppLinkData.connectUrl)
        this.actionAppLinkData = actionAppLinkData
        when {
            connections.isEmpty() -> {
                showActionError(appContext.getString(R.string.errors_actions_no_connections_link_app))
            }
            connections.size == 1 -> {
                this.connectionAndKey = connections.firstOrNull()?.let {
                    keyStoreManager.createConnectionAndKeyModel(it)
                }
                if (connectionAndKey == null) viewMode = ViewMode.ACTION_ERROR
            }
            else -> {
                val result = connections.convertConnectionsToViewModels(
                    context = appContext
                )
                viewMode = ViewMode.SELECT
                showConnectionsSelectorFragmentEvent.postValue(ViewModelEvent(dataBundle(result)))
            }
        }
    }

    fun showConnectionSelector(connectionGuid: GUID) {
        this.connectionAndKey = connectionsRepository.getByGuid(connectionGuid)?.let {
            keyStoreManager.createConnectionAndKeyModel(it)
        }
        viewMode = if (connectionAndKey == null) ViewMode.ACTION_ERROR else ViewMode.START
        onViewCreated()
    }

    fun onViewCreated() {
        if (viewMode == ViewMode.START) {
            apiManager.sendAction(
                actionUUID = actionAppLinkData?.actionUUID ?: "",
                connectionAndKey = connectionAndKey ?: return,
                resultCallback = this
            )
            viewMode = ViewMode.PROCESSING
        }
        updateViewsContent()
    }

    fun onViewClick(viewId: Int) {
        if (viewId == R.id.actionView) {
            onCloseEvent.postUnitEvent()
            try {
                actionAppLinkData?.returnTo?.let {
                    if (it.isNotEmpty()) onOpenLinkEvent.postValue(ViewModelEvent(Uri.parse(it)))
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun updateViewsContent() {
        mainActionTextResId.postValue(R.string.actions_done)

        if (viewMode == ViewMode.ACTION_ERROR) {
            iconResId.postValue(R.drawable.ic_status_error)
            completeTitleResId.postValue(R.string.action_error_title)
            completeViewVisibility.postValue(View.VISIBLE)
            actionProcessingVisibility.postValue(View.GONE)
        } else {
            completeViewVisibility.postValue(View.GONE)
            actionProcessingVisibility.postValue(View.VISIBLE)
        }
    }

    private fun showActionError(errorMessage: String) {
        viewMode = ViewMode.ACTION_ERROR
        updateViewsContent()
        completeDescription.postValue(
            if (errorMessage.isEmpty()) appContext.getString(R.string.action_error_description) else errorMessage
        )
    }
}

enum class ViewMode {
    START, PROCESSING, ACTION_ERROR, SELECT
}
