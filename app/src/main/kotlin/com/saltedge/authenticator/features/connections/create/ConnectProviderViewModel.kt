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
package com.saltedge.authenticator.features.connections.create

import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Typeface.BOLD
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import androidx.core.text.set
import androidx.lifecycle.*
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.LOCATION_PERMISSION_REQUEST_CODE
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ConnectAppLinkData
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.getErrorMessage
import com.saltedge.authenticator.tools.postUnitEvent

class ConnectProviderViewModel(
    private val appContext: Context,
    private val locationManager: DeviceLocationManagerAbs,
    private val interactor: ConnectProviderInteractorAbs
) : ViewModel(), LifecycleObserver, ConnectProviderInteractorCallback {

    val backActionIconRes: MutableLiveData<ResId?> = MutableLiveData(R.drawable.ic_appbar_action_close)
    val statusIconRes: MutableLiveData<ResId> = MutableLiveData(R.drawable.ic_status_error)
    val completeTitle: MutableLiveData<SpannableString> = MutableLiveData(SpannableString(""))
    val completeDescription: MutableLiveData<String> = MutableLiveData("")
    val mainActionTextRes: MutableLiveData<ResId> = MutableLiveData(R.string.actions_try_again)
    val webViewVisibility = MutableLiveData<Int>(View.GONE)
    val progressViewVisibility = MutableLiveData<Int>(View.VISIBLE)
    val completeViewVisibility = MutableLiveData<Int>(View.GONE)
    var titleRes: ResId = R.string.connections_new_connection
        private set
    val onCloseEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onShowErrorEvent = MutableLiveData<ViewModelEvent<String>>()
    val onUrlChangedEvent = MutableLiveData<ViewModelEvent<String>>()
    val goBackEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onAskPermissionsEvent = MutableLiveData<ViewModelEvent<Unit>>()
    private var sessionFailMessage: String? = null
    private var viewMode: ViewMode = ViewMode.START_NEW_CONNECT

    fun setInitialData(initialConnectData: ConnectAppLinkData?, connectionGuid: GUID?) {
        interactor.contract = this
        interactor.setInitialData(initialConnectData, connectionGuid)

        viewMode = when {
            interactor.hasConnection -> ViewMode.START_RECONNECT
            interactor.hasConfigUrl -> ViewMode.START_NEW_CONNECT
            else -> ViewMode.COMPLETE_ERROR
        }
        titleRes = if (viewMode == ViewMode.START_NEW_CONNECT) R.string.connections_new_connection else R.string.actions_reconnect
    }

    fun onBackPress(webViewCanGoBack: Boolean?): Boolean {
        return (webViewIsVisible && webViewCanGoBack == true).also { goBackEvent.postUnitEvent() }
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
            && grantResults.any { it == PackageManager.PERMISSION_GRANTED }
        ) {
            locationManager.startLocationUpdates()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        when (viewMode) {
            ViewMode.START_NEW_CONNECT -> interactor.fetchScaConfiguration()
            ViewMode.START_RECONNECT -> interactor.requestCreateConnection()
            ViewMode.WEB_ENROLL -> loadWebRedirectUrl()
            else -> Unit
        }
        updateViewsContent()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        interactor.destroyConnectionIfNotAuthorized()
        interactor.contract = null
    }

    fun onViewClick(viewId: Int) {
        if (viewId == R.id.actionView) onCloseEvent.postUnitEvent()
    }

    fun onDialogActionIdClick(dialogActionId: Int) {
        if (dialogActionId == DialogInterface.BUTTON_POSITIVE) onCloseEvent.postUnitEvent()
    }

    fun onReturnToRedirect(url: String) {
        interactor.onReceiveReturnToUrl(url)
    }

    override fun onReceiveApiError(error: ApiErrorData) {
        onShowErrorEvent.postValue(ViewModelEvent(error.getErrorMessage(appContext)))
    }

    override fun onReceiveAuthenticationUrl() {
        viewMode = ViewMode.WEB_ENROLL
        updateViewsContent()
        loadWebRedirectUrl()
    }

    override fun onConnectionFailAuthentication(errorClass: String, errorMessage: String?) {
        viewMode = ViewMode.COMPLETE_ERROR
        sessionFailMessage = errorMessage
        updateViewsContent()
    }

    override fun onConnectionSuccessAuthentication() {
        viewMode = ViewMode.COMPLETE_SUCCESS
        updateViewsContent()
        checkGeolocationRequirements()
    }

    private val webViewIsVisible: Boolean
        get() = viewMode == ViewMode.WEB_ENROLL
    private val progressViewIsVisible: Boolean
        get() = viewMode == ViewMode.START_NEW_CONNECT || viewMode == ViewMode.START_RECONNECT
    private val completeViewIsVisible: Boolean
        get() = viewMode == ViewMode.COMPLETE_SUCCESS || viewMode == ViewMode.COMPLETE_ERROR
    private val ViewMode.isCompleteWithSuccess: Boolean
        get() = this == ViewMode.COMPLETE_SUCCESS
    private val completeIconRes: ResId
        get() = if (viewMode.isCompleteWithSuccess) R.drawable.ic_status_success else R.drawable.ic_status_error
    private val completeActionTextRes: ResId
        get() = if (viewMode.isCompleteWithSuccess) R.string.actions_done else R.string.actions_try_again

    private fun checkGeolocationRequirements() {
        interactor.geolocationRequired?.let {
            val permissionGranted: Boolean = locationManager.locationPermissionsGranted()
            if (permissionGranted) locationManager.startLocationUpdates()
            else onAskPermissionsEvent.postUnitEvent()
        }
    }

    private fun loadWebRedirectUrl() {
        onUrlChangedEvent.postValue(ViewModelEvent(interactor.authenticationUrl))
    }

    private fun updateViewsContent() {
        statusIconRes.postValue(completeIconRes)
        completeTitle.postValue(getCompleteTitle())
        completeDescription.postValue(getCompleteDescription())
        mainActionTextRes.postValue(completeActionTextRes)

        webViewVisibility.postValue(if (webViewIsVisible) View.VISIBLE else View.GONE)
        completeViewVisibility.postValue(if (completeViewIsVisible) View.VISIBLE else View.GONE)
        progressViewVisibility.postValue(if (progressViewIsVisible) View.VISIBLE else View.GONE)
        backActionIconRes.postValue(if (progressViewIsVisible || completeViewIsVisible) null else R.drawable.ic_appbar_action_close)
    }

    private fun getCompleteTitle(): SpannableString {
        return if (viewMode.isCompleteWithSuccess) {
            val connectionName = interactor.connectionName
            val resultString = appContext.getString(R.string.connect_status_provider_success).format(connectionName)
            val start = resultString.indexOf(connectionName)
            val end = start + connectionName.length
            val result = SpannableString(resultString)
            result[start, end] = StyleSpan(BOLD)
            result
        } else {
            SpannableString(appContext.getString(R.string.errors_connection_failed))
        }
    }

    private fun getCompleteDescription(): String {
        return if (viewMode.isCompleteWithSuccess) {
            appContext.getString(R.string.connect_status_provider_success_description)
        } else {
            sessionFailMessage ?: appContext.getString(
                R.string.errors_connection_failed_description
            )
        }
    }
}

enum class ViewMode {
    START_NEW_CONNECT, START_RECONNECT, WEB_ENROLL, COMPLETE_SUCCESS, COMPLETE_ERROR;
}
