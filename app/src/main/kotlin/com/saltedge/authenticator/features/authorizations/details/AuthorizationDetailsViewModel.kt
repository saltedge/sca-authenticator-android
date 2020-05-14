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
package com.saltedge.authenticator.features.authorizations.details

import android.content.Context
import androidx.lifecycle.*
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.authorizations.common.AuthorizationViewModel
import com.saltedge.authenticator.features.authorizations.common.ViewMode
import com.saltedge.authenticator.features.authorizations.common.createConnectionAndKey
import com.saltedge.authenticator.features.authorizations.common.toAuthorizationViewModel
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.contract.ConfirmAuthorizationListener
import com.saltedge.authenticator.sdk.contract.FetchAuthorizationContract
import com.saltedge.authenticator.sdk.model.AuthorizationID
import com.saltedge.authenticator.sdk.model.ConnectionID
import com.saltedge.authenticator.sdk.model.EncryptedData
import com.saltedge.authenticator.sdk.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.error.*
import com.saltedge.authenticator.sdk.model.response.ConfirmDenyResponseData
import com.saltedge.authenticator.sdk.polling.SingleAuthorizationPollingService
import com.saltedge.authenticator.sdk.tools.crypt.CryptoToolsAbs
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import org.joda.time.DateTime

class AuthorizationDetailsViewModel(
    private val appContext: Context,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val cryptoTools: CryptoToolsAbs,
    private val keyStoreManager: KeyStoreManagerAbs,
    private val apiManager: AuthenticatorApiManagerAbs
) : ViewModel(),
    LifecycleObserver,
    FetchAuthorizationContract,
    ConfirmAuthorizationListener
{
    val onErrorEvent = MutableLiveData<ViewModelEvent<String>>()
    val onCloseViewEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onTimeUpdateEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val authorizationData = MutableLiveData<AuthorizationViewModel>()

    private var connectionAndKey: ConnectionAndKey? = null
    private var pollingService: SingleAuthorizationPollingService = apiManager.createSingleAuthorizationPollingService()
    private val viewMode: ViewMode
        get() = authorizationData.value?.viewMode ?: ViewMode.LOADING
    private val modelHasFinalMode: Boolean
        get() = authorizationData.value?.hasFinalMode ?: false
    private var authorizationId: String? = null

    fun bindLifecycleObserver(lifecycle: Lifecycle) {
        lifecycle.let {
            it.removeObserver(this)
            it.addObserver(this)
            it.removeObserver(pollingService)
            it.addObserver(pollingService)
        }
        pollingService.contract = this
    }

    fun setInitialData(identifier: AuthorizationIdentifier?) {
        connectionAndKey = createConnectionAndKey(
            connectionID = identifier?.connectionID ?: "",
            repository = connectionsRepository,
            keyStoreManager = keyStoreManager
        )
        this.authorizationId = identifier?.authorizationID
        val viewMode = if (connectionAndKey == null || authorizationId.isNullOrEmpty()) {
            ViewMode.UNAVAILABLE
        } else {
            ViewMode.LOADING
        }
        authorizationData.value = AuthorizationViewModel(
            authorizationID = "",
            authorizationCode = "",
            title = "",
            description = "",
            validSeconds = 0,
            endTime = DateTime(0L),
            startTime = DateTime(0L),
            connectionID = "",
            connectionName = "",
            connectionLogoUrl = "",
            viewMode = viewMode
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onFragmentResume() {
        if (viewMode === ViewMode.LOADING || viewMode === ViewMode.DEFAULT) startPolling()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onFragmentPause() {
        stopPolling()
    }

    fun onViewClick(viewId: Int) {
        when (viewId) {
            R.id.positiveActionView -> sendConfirmRequest()
            R.id.negativeActionView -> sendDenyRequest()
        }
    }

    fun onTimerTick() {
        authorizationData.value?.also { model ->
            when {
                model.shouldBeSetTimeOutMode -> updateToFinalViewMode(ViewMode.TIME_OUT)
                model.shouldBeDestroyed -> onCloseViewEvent.postValue(ViewModelEvent(Unit))
                !model.ignoreTimeUpdate -> onTimeUpdateEvent.postValue(ViewModelEvent(Unit))
            }
        }
    }

    override fun getConnectionDataForAuthorizationPolling(): ConnectionAndKey? = this.connectionAndKey

    override fun onFetchAuthorizationResult(result: EncryptedData?, error: ApiErrorData?) {
        result?.let { processEncryptedAuthorizationResult(it) }
            ?: error?.let { processFetchAuthorizationError(it) }
            ?: updateToFinalViewMode(ViewMode.UNAVAILABLE)
    }

    override fun onConfirmDenySuccess(result: ConfirmDenyResponseData, connectionID: ConnectionID) {
        val newViewMode = if (result.success == true) {
            when (viewMode) {
                ViewMode.CONFIRM_PROCESSING -> ViewMode.CONFIRM_SUCCESS
                ViewMode.DENY_PROCESSING -> ViewMode.DENY_SUCCESS
                else -> ViewMode.ERROR
            }
        } else {
            startPolling()
            ViewMode.DEFAULT
        }
        updateViewMode(newViewMode)
    }

    override fun onConfirmDenyFailure(
        error: ApiErrorData,
        connectionID: ConnectionID,
        authorizationID: AuthorizationID
    ) {
        processFetchAuthorizationError(error)
    }

    private fun processEncryptedAuthorizationResult(result: EncryptedData) {
        if (viewMode.isProcessingMode()) return

        cryptoTools.decryptAuthorizationData(
            encryptedData = result,
            rsaPrivateKey = connectionAndKey?.key
        )?.let {
            val newViewModel = it.toAuthorizationViewModel(
                connection = connectionAndKey?.connection ?: return
            )
            if (!modelHasFinalMode && authorizationData.value != newViewModel) {
                authorizationData.postValue(newViewModel)
            }
        } ?: updateToFinalViewMode(ViewMode.UNAVAILABLE)
    }

    private fun processFetchAuthorizationError(error: ApiErrorData) {
        when {
            error.isConnectionNotFound() -> {
                connectionAndKey?.connection?.accessToken?.let {
                    connectionsRepository.invalidateConnectionsByTokens(accessTokens = listOf(it))
                }
                updateToFinalViewMode(ViewMode.ERROR)
            }
            shouldSetUnavailableState(error) -> updateToFinalViewMode(ViewMode.UNAVAILABLE)
            !error.isConnectivityError() -> {
                if (viewMode != ViewMode.ERROR) {
                    onErrorEvent.postValue(ViewModelEvent(error.getErrorMessage(appContext)))
                    updateToFinalViewMode(ViewMode.ERROR)
                }
            }
            else -> onErrorEvent.postValue(ViewModelEvent(error.getErrorMessage(appContext)))
        }
    }

    private fun shouldSetUnavailableState(error: ApiErrorData): Boolean {
        return error.isAuthorizationNotFound() && (viewMode === ViewMode.LOADING || viewMode === ViewMode.DEFAULT)
    }

    private fun sendConfirmRequest() {
        onStartConfirmDenyFlow(viewMode = ViewMode.CONFIRM_PROCESSING)
        apiManager.confirmAuthorization(
            connectionAndKey = connectionAndKey ?: return,
            authorizationId = authorizationData.value?.authorizationID ?: return,
            authorizationCode = authorizationData.value?.authorizationCode,
            resultCallback = this
        )
    }

    private fun sendDenyRequest() {
        onStartConfirmDenyFlow(ViewMode.DENY_PROCESSING)
        apiManager.denyAuthorization(
            connectionAndKey = connectionAndKey ?: return,
            authorizationId = authorizationData.value?.authorizationID ?: return,
            authorizationCode = authorizationData.value?.authorizationCode,
            resultCallback = this
        )
    }

    private fun onStartConfirmDenyFlow(viewMode: ViewMode) {
        stopPolling()
        updateViewMode(viewMode)
    }

    private fun updateToFinalViewMode(newViewMode: ViewMode) {
        stopPolling()
        updateViewMode(newViewMode)
        onTimeUpdateEvent.postValue(ViewModelEvent(Unit))
    }

    private fun updateViewMode(newViewMode: ViewMode) {
        authorizationData.value?.let {
            it.setNewViewMode(newViewMode = newViewMode)
            authorizationData.postValue(it)
        }
    }

    private fun startPolling() {
        if (viewMode != ViewMode.UNAVAILABLE) {
            authorizationId?.let {
                pollingService.contract = this
                pollingService.start(authorizationId = it)
            }
        }
    }

    private fun stopPolling() {
        pollingService.contract = null
        pollingService.stop()
    }
}
