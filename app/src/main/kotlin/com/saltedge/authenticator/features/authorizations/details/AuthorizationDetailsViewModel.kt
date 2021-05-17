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
import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.core.api.model.DescriptionData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.isAuthorizationNotFound
import com.saltedge.authenticator.core.api.model.error.isConnectionNotFound
import com.saltedge.authenticator.core.api.model.error.isConnectivityError
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.AuthorizationStatus
import com.saltedge.authenticator.features.authorizations.common.toAuthorizationItemViewModel
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.createRichConnection
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.api.model.EncryptedData
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.sdk.api.model.response.ConfirmDenyResponseData
import com.saltedge.authenticator.sdk.contract.ConfirmAuthorizationListener
import com.saltedge.authenticator.sdk.polling.FetchAuthorizationContract
import com.saltedge.authenticator.sdk.polling.SingleAuthorizationPollingService
import com.saltedge.authenticator.sdk.tools.CryptoToolsV1Abs
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.getErrorMessage
import com.saltedge.authenticator.tools.postUnitEvent
import org.joda.time.DateTime

class AuthorizationDetailsViewModel(
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val cryptoTools: CryptoToolsV1Abs,
    private val keyStoreManager: KeyManagerAbs,
    private val apiManager: AuthenticatorApiManagerAbs,
    private val locationManager: DeviceLocationManagerAbs
) : ViewModel(),
    LifecycleObserver,
    FetchAuthorizationContract,
    ConfirmAuthorizationListener
{
    val onErrorEvent = MutableLiveData<ViewModelEvent<ApiErrorData>>()
    val onCloseAppEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onCloseViewEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onTimeUpdateEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val authorizationModel = MutableLiveData<AuthorizationItemViewModel>()
    private var closeAppOnBackPress: Boolean = true
    var titleRes: ResId = R.string.authorization_feature_title//TODO TEST
        private set
    private var richConnection: RichConnection? = null
    private var pollingService: SingleAuthorizationPollingService = apiManager.createSingleAuthorizationPollingService()
    private val viewMode: AuthorizationStatus
        get() = authorizationModel.value?.status ?: AuthorizationStatus.LOADING
    private val modelHasFinalMode: Boolean
        get() = authorizationModel.value?.hasFinalStatus ?: false

    fun bindLifecycleObserver(lifecycle: Lifecycle) {
        lifecycle.let {
            it.removeObserver(this)
            it.addObserver(this)
            it.removeObserver(pollingService)
            it.addObserver(pollingService)
        }
        pollingService.contract = this
    }

    fun setInitialData(
        identifier: AuthorizationIdentifier?,
        closeAppOnBackPress: Boolean?,
        titleRes: ResId?
    ) {
        this.closeAppOnBackPress = closeAppOnBackPress ?: true
        this.titleRes = titleRes ?: R.string.authorization_feature_title
        if (this.titleRes == 0) this.titleRes = R.string.authorization_feature_title
        richConnection = createRichConnection(
            connectionID = identifier?.connectionID ?: "",
            repository = connectionsRepository,
            keyStoreManager = keyStoreManager
        )
        val status = if (richConnection == null || identifier?.authorizationID.isNullOrEmpty()) {
            AuthorizationStatus.UNAVAILABLE
        } else {
            AuthorizationStatus.LOADING
        }
        authorizationModel.value = AuthorizationItemViewModel(
            authorizationID = identifier?.authorizationID ?: "",
            authorizationCode = "",
            title = "",
            description = DescriptionData(),
            validSeconds = 0,
            endTime = DateTime(0L),
            startTime = DateTime(0L),
            connectionID = identifier?.connectionID ?: "",
            connectionName = "",
            connectionLogoUrl = "",
            status = status,
            apiVersion = "1"
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onFragmentResume() {
        if (viewMode === AuthorizationStatus.LOADING || viewMode === AuthorizationStatus.PENDING) startPolling()
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
        authorizationModel.value?.also { model ->
            when {
                model.shouldBeSetTimeOutMode -> updateToFinalViewMode(AuthorizationStatus.TIME_OUT)
                model.shouldBeDestroyed -> closeView()
                !model.ignoreTimeUpdate -> onTimeUpdateEvent.postUnitEvent()
            }
        }
    }

    fun onBackPress(): Boolean {
        closeView()
        return true
    }

    override fun getConnectionDataForAuthorizationPolling(): RichConnection? = this.richConnection

    override fun onFetchAuthorizationResult(result: EncryptedData?, error: ApiErrorData?) {
        result?.let { processEncryptedAuthorizationResult(it) }
            ?: error?.let { processFetchAuthorizationError(it) }
            ?: updateToFinalViewMode(AuthorizationStatus.UNAVAILABLE)
    }

    override fun onConfirmDenySuccess(result: ConfirmDenyResponseData, connectionID: ID) {
        val newViewMode = if (result.success == true) {
            when (viewMode) {
                AuthorizationStatus.CONFIRM_PROCESSING -> AuthorizationStatus.CONFIRMED
                AuthorizationStatus.DENY_PROCESSING -> AuthorizationStatus.DENIED
                else -> AuthorizationStatus.ERROR
            }
        } else {
            startPolling()
            AuthorizationStatus.PENDING
        }
        updateViewMode(newViewMode)
    }

    override fun onConfirmDenyFailure(
        error: ApiErrorData,
        connectionID: ID,
        authorizationID: ID
    ) {
        processFetchAuthorizationError(error)
    }

    private fun processEncryptedAuthorizationResult(result: EncryptedData) {
        if (viewMode.isProcessingMode()) return

        cryptoTools.decryptAuthorizationData(
            encryptedData = result,
            rsaPrivateKey = richConnection?.private
        )?.let {
            val newViewModel = it.toAuthorizationItemViewModel(
                connection = richConnection?.connection ?: return
            )
            if (!modelHasFinalMode && authorizationModel.value != newViewModel) {
                if (newViewModel == null) {
                    updateToFinalViewMode(AuthorizationStatus.ERROR)
                } else {
                    authorizationModel.postValue(newViewModel)
                }
            }
        } ?: updateToFinalViewMode(AuthorizationStatus.UNAVAILABLE)
    }

    private fun processFetchAuthorizationError(error: ApiErrorData) {
        when {
            error.isConnectionNotFound() -> {
                richConnection?.connection?.accessToken?.let {
                    connectionsRepository.invalidateConnectionsByTokens(accessTokens = listOf(it))
                }
                updateToFinalViewMode(AuthorizationStatus.ERROR)
            }
            shouldSetUnavailableState(error) -> updateToFinalViewMode(AuthorizationStatus.UNAVAILABLE)
            !error.isConnectivityError() -> {
                if (viewMode != AuthorizationStatus.ERROR) {
                    onErrorEvent.postValue(ViewModelEvent(error))
                    updateToFinalViewMode(AuthorizationStatus.ERROR)
                }
            }
            else -> onErrorEvent.postValue(ViewModelEvent(error))
        }
    }

    private fun shouldSetUnavailableState(error: ApiErrorData): Boolean {
        return error.isAuthorizationNotFound() && (viewMode === AuthorizationStatus.LOADING || viewMode === AuthorizationStatus.PENDING)
    }

    private fun sendConfirmRequest() {
        onStartConfirmDenyFlow(viewMode = AuthorizationStatus.CONFIRM_PROCESSING)
        apiManager.confirmAuthorization(
            connectionAndKey = richConnection ?: return,
            authorizationId = authorizationModel.value?.authorizationID ?: return,
            authorizationCode = authorizationModel.value?.authorizationCode,
            geolocation = locationManager.locationDescription,
            authorizationType = AppTools.lastUnlockType.description,
            resultCallback = this
        )
    }

    private fun sendDenyRequest() {
        onStartConfirmDenyFlow(AuthorizationStatus.DENY_PROCESSING)
        apiManager.denyAuthorization(
            connectionAndKey = richConnection ?: return,
            authorizationId = authorizationModel.value?.authorizationID ?: return,
            authorizationCode = authorizationModel.value?.authorizationCode,
            geolocation = locationManager.locationDescription,
            authorizationType = AppTools.lastUnlockType.description,
            resultCallback = this
        )
    }

    private fun onStartConfirmDenyFlow(viewMode: AuthorizationStatus) {
        stopPolling()
        updateViewMode(viewMode)
    }

    private fun updateToFinalViewMode(newViewMode: AuthorizationStatus) {
        stopPolling()
        updateViewMode(newViewMode)
        onTimeUpdateEvent.postUnitEvent()
    }

    private fun updateViewMode(newViewMode: AuthorizationStatus) {
        authorizationModel.value?.let {
            it.setNewStatus(newStatus = newViewMode)
            authorizationModel.postValue(it)
        }
    }

    private fun startPolling() {
        if (viewMode != AuthorizationStatus.UNAVAILABLE) {
            authorizationModel.value?.authorizationID?.let {
                pollingService.contract = this
                pollingService.start(authorizationId = it)
            }
        }
    }

    private fun stopPolling() {
        pollingService.contract = null
        pollingService.stop()
    }

    private fun closeView() {
        if (closeAppOnBackPress) onCloseAppEvent.postUnitEvent()
        else onCloseViewEvent.postUnitEvent()
    }
}
