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

import androidx.lifecycle.*
import com.saltedge.authenticator.R
import com.saltedge.authenticator.core.api.model.DescriptionData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.AuthorizationStatus
import com.saltedge.authenticator.features.authorizations.common.computeConfirmedStatus
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.postUnitEvent
import kotlinx.coroutines.CoroutineScope
import org.joda.time.DateTime

class AuthorizationDetailsViewModel(
    private val interactor: AuthorizationDetailsInteractor
) : ViewModel(),
    LifecycleObserver,
    AuthorizationDetailsInteractorCallback
{
    val onErrorEvent = MutableLiveData<ViewModelEvent<ApiErrorData>>()
    val onCloseAppEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onCloseViewEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onTimeUpdateEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val authorizationModel = MutableLiveData<AuthorizationItemViewModel>()
    private var closeAppOnBackPress: Boolean = true
    var titleRes: ResId = R.string.authorization_feature_title//TODO TEST
        private set
    private val currentStatus: AuthorizationStatus
        get() = authorizationModel.value?.status ?: AuthorizationStatus.LOADING
    private val authorizationHasFinalMode: Boolean
        get() = authorizationModel.value?.hasFinalStatus ?: false

    init {
        interactor.contract = this
    }

    fun bindLifecycleObserver(lifecycle: Lifecycle) {
        lifecycle.let {
            it.removeObserver(this)
            it.addObserver(this)
        }
        interactor.bindLifecycleObserver(lifecycle)
    }

    fun setInitialData(
        identifier: AuthorizationIdentifier?,
        closeAppOnBackPress: Boolean?,
        titleRes: ResId?
    ) {
        this.closeAppOnBackPress = closeAppOnBackPress ?: true
        this.titleRes = titleRes ?: R.string.authorization_feature_title
        if (this.titleRes == 0) this.titleRes = R.string.authorization_feature_title
        interactor.setInitialData(identifier?.connectionID ?: "")
        val status = if (interactor.richConnection == null || identifier == null || !identifier.hasAuthorizationID) {
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
        startPolling()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onFragmentPause() {
        interactor.stopPolling()
    }

    fun onViewClick(viewId: Int) {
        when (viewId) {
            R.id.positiveActionView -> {
                authorizationModel.value?.let { updateAuthorization(model = it, confirm = true) }
            }
            R.id.negativeActionView -> {
                authorizationModel.value?.let { updateAuthorization(model = it, confirm = false) }
            }
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

//    override fun onFetchAuthorizationResult(result: EncryptedData?, error: ApiErrorData?) {
//        result?.let { processEncryptedAuthorizationResult(it) }
//            ?: error?.let { processFetchAuthorizationError(it) }
//            ?: updateToFinalViewMode(AuthorizationStatus.UNAVAILABLE)
//    }

//    override fun onConfirmDenySuccess(result: ConfirmDenyResponseData, connectionID: ID) {
//        val newViewMode = if (result.success == true) {
//            when (currentStatus) {
//                AuthorizationStatus.CONFIRM_PROCESSING -> AuthorizationStatus.CONFIRMED
//                AuthorizationStatus.DENY_PROCESSING -> AuthorizationStatus.DENIED
//                else -> AuthorizationStatus.ERROR
//            }
//        } else {
//            startPolling()
//            AuthorizationStatus.PENDING
//        }
//        updateViewMode(newViewMode)
//    }

    override fun onAuthorizationReceived(
        data: AuthorizationItemViewModel?,
        newModelsApiVersion: String
    ) {
        if (currentStatus.isProcessingMode()) return//skip polling result if confirm/deny is in progress
        if (!authorizationHasFinalMode && authorizationModel.value != data) {
            if (data == null) updateToFinalViewMode(AuthorizationStatus.ERROR)
            else authorizationModel.postValue(data)
        }
    }

    override fun onConfirmDenySuccess(newStatus: AuthorizationStatus?) {
        updateAuthorizationStatus(newStatus = newStatus ?: currentStatus.computeConfirmedStatus())
    }

    override fun onConnectionNotFoundError() {
        updateToFinalViewMode(AuthorizationStatus.ERROR)
    }

    override fun onAuthorizationNotFoundError() {
        if ((currentStatus === AuthorizationStatus.LOADING || currentStatus === AuthorizationStatus.PENDING)) {
            updateToFinalViewMode(AuthorizationStatus.UNAVAILABLE)
        }
    }

    override fun onConnectivityError(error: ApiErrorData) {
        onErrorEvent.postValue(ViewModelEvent(error))
    }

    override fun onError(error: ApiErrorData) {
        if (currentStatus != AuthorizationStatus.ERROR) {
            onErrorEvent.postValue(ViewModelEvent(error))
            updateToFinalViewMode(AuthorizationStatus.ERROR)
        }
    }

    override val coroutineScope: CoroutineScope
        get() = viewModelScope

//    private fun processEncryptedAuthorizationResult(result: EncryptedData) {
//        if (viewMode.isProcessingMode()) return
//
//        cryptoTools.decryptAuthorizationData(
//            encryptedData = result,
//            rsaPrivateKey = richConnection?.private
//        )?.let {
//            val newViewModel = it.toAuthorizationItemViewModel(
//                connection = richConnection?.connection ?: return
//            )
//            if (!authorizationHasFinalMode && authorizationModel.value != newViewModel) {
//                if (newViewModel == null) {
//                    updateToFinalViewMode(AuthorizationStatus.ERROR)
//                } else {
//                    authorizationModel.postValue(newViewModel)
//                }
//            }
//        } ?: updateToFinalViewMode(AuthorizationStatus.UNAVAILABLE)
//    }

//    private fun processFetchAuthorizationError(error: ApiErrorData) {
//        when {
//            error.isConnectionNotFound() -> {
//                richConnection?.connection?.accessToken?.let {
//                    connectionsRepository.invalidateConnectionsByTokens(accessTokens = listOf(it))
//                }
//                updateToFinalViewMode(AuthorizationStatus.ERROR)
//            }
//            shouldSetUnavailableState(error) -> updateToFinalViewMode(AuthorizationStatus.UNAVAILABLE)
//            !error.isConnectivityError() -> {
//                if (viewMode != AuthorizationStatus.ERROR) {
//                    onErrorEvent.postValue(ViewModelEvent(error))
//                    updateToFinalViewMode(AuthorizationStatus.ERROR)
//                }
//            }
//            else -> onErrorEvent.postValue(ViewModelEvent(error))
//        }
//    }

//    private fun shouldSetUnavailableState(error: ApiErrorData): Boolean {
//        return error.isAuthorizationNotFound() && (viewMode === AuthorizationStatus.LOADING || viewMode === AuthorizationStatus.PENDING)
//    }

//    private fun sendConfirmRequest() {
//        onStartConfirmDenyFlow(viewMode = AuthorizationStatus.CONFIRM_PROCESSING)
//        apiManager.confirmAuthorization(
//            connectionAndKey = richConnection ?: return,
//            authorizationId = authorizationModel.value?.authorizationID ?: return,
//            authorizationCode = authorizationModel.value?.authorizationCode,
//            geolocation = locationManager.locationDescription,
//            authorizationType = AppTools.lastUnlockType.description,
//            resultCallback = this
//        )
//    }
//
//    private fun sendDenyRequest() {
//        onStartConfirmDenyFlow(AuthorizationStatus.DENY_PROCESSING)
//        apiManager.denyAuthorization(
//            connectionAndKey = richConnection ?: return,
//            authorizationId = authorizationModel.value?.authorizationID ?: return,
//            authorizationCode = authorizationModel.value?.authorizationCode,
//            geolocation = locationManager.locationDescription,
//            authorizationType = AppTools.lastUnlockType.description,
//            resultCallback = this
//        )
//    }

//    private fun onStartConfirmDenyFlow(viewMode: AuthorizationStatus) {
//        stopPolling()
//        updateViewMode(viewMode)
//    }

    private fun updateAuthorization(model: AuthorizationItemViewModel, confirm: Boolean) {
        updateAuthorizationStatus(if (confirm) AuthorizationStatus.CONFIRM_PROCESSING else AuthorizationStatus.DENY_PROCESSING)
        interactor.updateAuthorization(
            authorizationID = model.authorizationID,
            authorizationCode = model.authorizationCode,
            confirm = confirm
        )
    }

    private fun updateToFinalViewMode(newStatus: AuthorizationStatus) {
        updateAuthorizationStatus(newStatus)
        onTimeUpdateEvent.postUnitEvent()
    }

    private fun updateAuthorizationStatus(newStatus: AuthorizationStatus) {
        authorizationModel.value?.let {
            it.setNewStatus(newStatus = newStatus)
            authorizationModel.postValue(it)
        }
    }

    private fun startPolling() {
        if (currentStatus != AuthorizationStatus.UNAVAILABLE) {
            authorizationModel.value?.authorizationID?.let {
                interactor.startPolling(authorizationID = it)
            }
        }
    }

    private fun closeView() {
        if (closeAppOnBackPress) onCloseAppEvent.postUnitEvent()
        else onCloseViewEvent.postUnitEvent()
    }
}
