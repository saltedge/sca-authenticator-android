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
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.AuthorizationStatus
import com.saltedge.authenticator.features.authorizations.common.BaseAuthorizationViewModel
import com.saltedge.authenticator.features.authorizations.common.computeConfirmedStatus
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.postUnitEvent
import kotlinx.coroutines.CoroutineScope
import org.joda.time.DateTime

class AuthorizationDetailsViewModel(
    private val interactorV1: AuthorizationDetailsInteractorAbs,
    private val interactorV2: AuthorizationDetailsInteractorAbs,
    private val locationManager: DeviceLocationManagerAbs
) : BaseAuthorizationViewModel(locationManager),
    LifecycleObserver,
    AuthorizationDetailsInteractorCallback {

    val onErrorEvent = MutableLiveData<ViewModelEvent<ApiErrorData>>()
    val onCloseAppEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onCloseViewEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onTimeUpdateEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val authorizationModel = MutableLiveData<AuthorizationItemViewModel>()
    var titleRes: ResId = R.string.authorization_feature_title
        private set
    override val coroutineScope: CoroutineScope
        get() = viewModelScope
    private lateinit var interactor: AuthorizationDetailsInteractorAbs
    private var closeAppOnBackPress: Boolean = true
    private val currentStatus: AuthorizationStatus
        get() = authorizationModel.value?.status ?: AuthorizationStatus.LOADING
    private val authorizationHasFinalMode: Boolean
        get() = authorizationModel.value?.hasFinalStatus ?: false

    fun setInitialData(
        identifier: AuthorizationIdentifier?,
        closeAppOnBackPress: Boolean?,
        titleRes: ResId?
    ) {
        this.closeAppOnBackPress = closeAppOnBackPress ?: true
        this.titleRes = titleRes ?: R.string.authorization_feature_title
        if (this.titleRes == 0) this.titleRes = R.string.authorization_feature_title

        initInteractor(connectionID = identifier?.connectionID ?: "")

        val status = if (interactor.noConnection || identifier == null || !identifier.hasAuthorizationID) {
                AuthorizationStatus.UNAVAILABLE
            } else {
                AuthorizationStatus.LOADING
            }
        createInitialItem(identifier, status, interactor.connectionApiVersion)
    }

    fun bindLifecycleObserver(lifecycle: Lifecycle) {
        lifecycle.let {
            it.removeObserver(this)
            it.addObserver(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onFragmentResume() {
        startPolling()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onFragmentPause() {
        interactor.stopPolling()
    }

    fun onViewClick(itemViewId: Int) {
        onViewItemClick(itemViewId, authorizationModel.value)
    }

    fun onTimerTick() {
        authorizationModel.value?.also { model ->
            when {
                model.shouldBeSetTimeOutMode -> {
                    interactor.stopPolling()
                    updateToFinalViewMode(AuthorizationStatus.TIME_OUT)
                }
                model.shouldBeDestroyed -> closeView()
                !model.ignoreTimeUpdate -> onTimeUpdateEvent.postUnitEvent()
            }
        }
    }

    fun onBackPress(): Boolean {
        closeView()
        return true
    }

    override fun onAuthorizationReceived(data: AuthorizationItemViewModel?, newModelApiVersion: String) {
        if (currentStatus.isProcessing()) return//skip polling result if confirm/deny is in progress
        if (!authorizationHasFinalMode && authorizationModel.value != data) {
            if (data == null) updateToFinalViewMode(AuthorizationStatus.ERROR)
            else authorizationModel.postValue(data)
        }
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

    override fun onConfirmDenySuccess(newStatus: AuthorizationStatus?) {
        updateAuthorizationStatus(newStatus = newStatus ?: currentStatus.computeConfirmedStatus())
    }

    override fun updateAuthorization(item: AuthorizationItemViewModel, confirm: Boolean) {
        updateAuthorizationStatus(if (confirm) AuthorizationStatus.CONFIRM_PROCESSING else AuthorizationStatus.DENY_PROCESSING)
        interactor.updateAuthorization(
            authorizationID = item.authorizationID,
            authorizationCode = item.authorizationCode,
            confirm = confirm,
            locationDescription = locationManager.locationDescription
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
        val authorizationID = authorizationModel.value?.authorizationID ?: return
        if (currentStatus != AuthorizationStatus.UNAVAILABLE && !currentStatus.isFinal()) {
            interactor.startPolling(authorizationID = authorizationID)
        }
    }

    private fun closeView() {
        if (closeAppOnBackPress) onCloseAppEvent.postUnitEvent()
        else onCloseViewEvent.postUnitEvent()
    }

    private fun initInteractor(connectionID: ID) {
        interactorV2.setInitialData(connectionID)
        interactor = if (interactorV2.connectionApiVersion == API_V2_VERSION) interactorV2
        else interactorV1.apply { setInitialData(connectionID) }
        interactor.contract = this
    }

    private fun createInitialItem(
        identifier: AuthorizationIdentifier?,
        status: AuthorizationStatus,
        apiVersion: String?
    ) {
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
            apiVersion = apiVersion ?: API_V1_VERSION,
            geolocationRequired = false
        )
    }
}
