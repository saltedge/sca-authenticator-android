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
import android.content.DialogInterface
import android.view.View
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.connections.common.ConnectionViewModel
import com.saltedge.authenticator.features.connections.list.convertConnectionsToViewModels
import com.saltedge.authenticator.models.ViewModelEvent
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

class SubmitActionViewModel @Inject constructor(
    private val appContext: Context,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyStoreManagerAbs,
    private val apiManager: AuthenticatorApiManagerAbs
) : ViewModel(), LifecycleObserver, ActionSubmitListener {

    private var viewMode: ViewMode = ViewMode.START
    private var actionAppLinkData: ActionAppLinkData? = null
    private var connectionAndKey: ConnectionAndKey? = null

    var onCloseEvent = MutableLiveData<ViewModelEvent<Unit>>()
        private set
    var onShowErrorEvent = MutableLiveData<ViewModelEvent<String>>()
        private set
    var onOpenLinkEvent = MutableLiveData<ViewModelEvent<String>>()
        private set
    var showConnectionsSelectorFragmentEvent = MutableLiveData<List<ConnectionViewModel>>()
        private set
    var showSubmitActionFragmentEvent = MutableLiveData<ViewModelEvent<ActionAppLinkData>>()
        private set
    var setResultAuthorizationIdentifier = MutableLiveData<AuthorizationIdentifier>()

    val iconResId: MutableLiveData<Int> = MutableLiveData(R.drawable.ic_status_error)
    val completeTitleResId: MutableLiveData<Int> = MutableLiveData(R.string.action_error_title)
    val completeDescriptionResId: MutableLiveData<Int> = MutableLiveData(R.string.action_error_description)
    val mainActionTextResId: MutableLiveData<Int> = MutableLiveData(R.string.actions_try_again)

    var completeViewVisibility = MutableLiveData<Int>()
    var actionProcessingVisibility = MutableLiveData<Int>()

    override fun onActionInitFailure(error: ApiErrorData) {
        showActionError(error.getErrorMessage(appContext))
    }

    override fun onActionInitSuccess(response: SubmitActionResponseData) {
        val authorizationID = response.authorizationId ?: ""
        val connectionID = response.connectionId ?: ""
        if (response.success == true && authorizationID.isNotEmpty() && connectionID.isNotEmpty()) {
            viewMode = ViewMode.ACTION_SUCCESS
            onCloseEvent.postValue(ViewModelEvent(Unit))
            setResultAuthorizationIdentifier.postValue(
                AuthorizationIdentifier(
                    authorizationID = authorizationID,
                    connectionID = connectionID
                )
            )
        } else {
            viewMode = if (response.success == true) ViewMode.ACTION_SUCCESS else ViewMode.ACTION_ERROR
            updateViewsContent()
        }
    }

    fun setInitialData(actionAppLinkData: ActionAppLinkData) {
        val connections = connectionsRepository.getByConnectUrl(actionAppLinkData.connectUrl)
        this.connectionAndKey = connections.firstOrNull()?.let {
            this.actionAppLinkData = actionAppLinkData
            keyStoreManager.createConnectionAndKeyModel(it)
        }
        if (connectionAndKey == null) {
            viewMode = ViewMode.ACTION_ERROR
        }

        when {
            connections.isEmpty() -> {
                showActionError(appContext.getString(R.string.connections_list_empty_title))
            }
            connections.size == 1 -> {
                showSubmitActionFragmentEvent.postValue(ViewModelEvent(actionAppLinkData))
            }
            else -> {
                val result = connections.convertConnectionsToViewModels(
                    context = appContext
                )
                showConnectionsSelectorFragmentEvent.postValue(result)
            }
        }
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
        if (viewId == R.id.mainActionView) {
            onCloseEvent.postValue(ViewModelEvent(Unit))
            val returnToUrl: String? = actionAppLinkData?.returnTo
            if (!returnToUrl.isNullOrEmpty()) onOpenLinkEvent.postValue(ViewModelEvent(returnToUrl))
        }
    }

    fun onDialogActionIdClick(dialogActionId: Int) {
        if (dialogActionId == DialogInterface.BUTTON_POSITIVE) onCloseEvent.postValue(ViewModelEvent(Unit))
    }

    private fun updateViewsContent() {
        iconResId.postValue(getIconResId())
        completeTitleResId.postValue(getCompleteTitle())
        completeDescriptionResId.postValue(getCompleteDescription())
        mainActionTextResId.postValue(getActionTextResId())

        completeViewVisibility.postValue(getCompleteViewVisibility())
        actionProcessingVisibility.postValue(getProgressViewVisibility())
    }

    private val ViewMode.isCompleteWithSuccess: Boolean
        get() = this == ViewMode.ACTION_SUCCESS

    private fun getIconResId(): Int {
        return if (viewMode.isCompleteWithSuccess) R.drawable.ic_status_success else R.drawable.ic_status_error
    }

    private fun getCompleteTitle(): Int {
        return if (viewMode.isCompleteWithSuccess) R.string.action_feature_title else R.string.action_error_title
    }

    private fun getCompleteDescription(): Int {
        return if (viewMode.isCompleteWithSuccess) R.string.action_feature_description else R.string.action_error_description
    }

    private fun getActionTextResId(): Int {
        return if (viewMode.isCompleteWithSuccess) android.R.string.ok else R.string.actions_try_again
    }

    private fun getCompleteViewVisibility(): Int {
        return if (viewMode == ViewMode.ACTION_SUCCESS || viewMode == ViewMode.ACTION_ERROR) View.VISIBLE else View.GONE
    }

    private fun getProgressViewVisibility(): Int {
        return if (viewMode == ViewMode.PROCESSING) View.VISIBLE else View.GONE
    }

    private fun showActionError(errorMessage: String) {
        viewMode = ViewMode.ACTION_ERROR
        updateViewsContent()
        onShowErrorEvent.postValue(ViewModelEvent(errorMessage))
    }
}

enum class ViewMode {
    START, PROCESSING, ACTION_SUCCESS, ACTION_ERROR
}
