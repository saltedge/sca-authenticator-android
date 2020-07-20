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
import android.graphics.Typeface.BOLD
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.webkit.URLUtil
import androidx.core.text.set
import androidx.lifecycle.*
import com.saltedge.authenticator.R
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.models.toConnection
import com.saltedge.authenticator.sdk.AuthenticatorApiManager
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.contract.ConnectionCreateListener
import com.saltedge.authenticator.sdk.contract.FetchProviderConfigurationListener
import com.saltedge.authenticator.sdk.model.ConnectionID
import com.saltedge.authenticator.sdk.model.GUID
import com.saltedge.authenticator.sdk.model.Token
import com.saltedge.authenticator.sdk.model.appLink.ConnectAppLinkData
import com.saltedge.authenticator.sdk.model.configuration.ProviderConfigurationData
import com.saltedge.authenticator.sdk.model.configuration.isValid
import com.saltedge.authenticator.sdk.model.connection.ConnectionStatus
import com.saltedge.authenticator.sdk.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.model.error.getErrorMessage
import com.saltedge.authenticator.sdk.model.response.CreateConnectionResponseData
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.sdk.tools.parseRedirect
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.postUnitEvent

class ConnectProviderViewModel(
    private val appContext: Context,
    private val preferenceRepository: PreferenceRepositoryAbs,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyStoreManagerAbs,
    private val apiManager: AuthenticatorApiManagerAbs
) : ViewModel(), LifecycleObserver, ConnectionCreateListener, FetchProviderConfigurationListener {

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

    private var connection = Connection()
    private var initialConnectData: ConnectAppLinkData? = null
    private var authenticateData: CreateConnectionResponseData? = null
    private var sessionFailMessage: String? = null
    private var viewMode: ViewMode = ViewMode.START_NEW_CONNECT

    var onCloseEvent = MutableLiveData<ViewModelEvent<Unit>>()
        private set
    var onShowErrorEvent = MutableLiveData<ViewModelEvent<String>>()
        private set
    var onUrlChangedEvent = MutableLiveData<ViewModelEvent<String>>()
        private set
    var goBackEvent = MutableLiveData<ViewModelEvent<Unit>>()
        private set

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        when (viewMode) {
            ViewMode.START_NEW_CONNECT -> performFetchConfigurationRequest()
            ViewMode.START_RECONNECT -> performCreateConnectionRequest()
            ViewMode.WEB_ENROLL -> loadWebRedirectUrl()
            else -> Unit
        }
        updateViewsContent()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        if (connection.guid.isNotEmpty() && connection.accessToken.isEmpty()) {
            keyStoreManager.deleteKeyPair(connection.guid)
        }
    }

    override fun fetchProviderConfigurationDataResult(
        result: ProviderConfigurationData?,
        error: ApiErrorData?
    ) {
        when {
            error != null -> onShowErrorEvent.postValue(
                ViewModelEvent(
                    error.getErrorMessage(
                        appContext
                    )
                )
            )
            result != null && result.isValid() -> {
                result.toConnection()?.let {
                    this.connection = it
                    performCreateConnectionRequest()
                }
                    ?: onShowErrorEvent.postValue(ViewModelEvent(appContext.getString(R.string.errors_unable_connect_provider)))
            }
            else -> onShowErrorEvent.postValue(ViewModelEvent(appContext.getString(R.string.errors_unable_connect_provider)))
        }
    }

    override fun onConnectionCreateSuccess(response: CreateConnectionResponseData) {
        val accessToken = response.accessToken
        val redirectUrl = response.redirectUrl
        if (accessToken?.isNotEmpty() == true) {
            authFinishedWithSuccess(
                connectionId = response.connectionId ?: "",
                accessToken = accessToken
            )
        } else if (redirectUrl?.isNotEmpty() == true) {
            if (redirectUrl.startsWith(AuthenticatorApiManager.authenticationReturnUrl)) {
                parseRedirect(
                    url = redirectUrl,
                    success = { connectionID, resultAccessToken ->
                        authFinishedWithSuccess(connectionID, resultAccessToken)
                    },
                    error = { errorClass, errorMessage ->
                        webAuthFinishError(errorClass, errorMessage)
                    }
                )
            } else if (URLUtil.isValidUrl(response.redirectUrl ?: "")) {
                connection.id = response.connectionId ?: ""
                authenticateData = response
                viewMode = ViewMode.WEB_ENROLL
                updateViewsContent()
                loadWebRedirectUrl()
            }
        }
    }

    override fun onConnectionCreateFailure(error: ApiErrorData) {
        onShowErrorEvent.postValue(ViewModelEvent(error.getErrorMessage(appContext)))
    }

    fun setInitialData(initialConnectData: ConnectAppLinkData?, connectionGuid: GUID?) {
        if (initialConnectData == null && connectionGuid == null) {
            viewMode = ViewMode.COMPLETE_ERROR
        } else if (connectionGuid != null) {
            this.connection = connectionsRepository.getByGuid(connectionGuid) ?: Connection()
            viewMode = ViewMode.START_RECONNECT
        } else {
            this.initialConnectData = initialConnectData
            viewMode = ViewMode.START_NEW_CONNECT
        }

        titleRes = if (this.connection.guid.isEmpty()) R.string.connections_new_connection
        else R.string.actions_reconnect
    }

    fun onViewClick(viewId: Int) {
        if (viewId == R.id.actionView) onCloseEvent.postUnitEvent()
    }

    fun onDialogActionIdClick(dialogActionId: Int) {
        if (dialogActionId == DialogInterface.BUTTON_POSITIVE) onCloseEvent.postUnitEvent()
    }

    fun authFinishedWithSuccess(connectionId: ConnectionID, accessToken: Token) {
        viewMode = ViewMode.COMPLETE_SUCCESS
        connection.id = connectionId
        connection.accessToken = accessToken
        connection.status = "${ConnectionStatus.ACTIVE}"
        if (connectionsRepository.connectionExists(connection)) {
            connectionsRepository.saveModel(connection)
        } else {
            connectionsRepository.fixNameAndSave(connection)
        }
        updateViewsContent()
    }

    fun webAuthFinishError(errorClass: String, errorMessage: String?) {
        viewMode = ViewMode.COMPLETE_ERROR
        sessionFailMessage = errorMessage
        updateViewsContent()
    }

    fun onBackPress(webViewCanGoBack: Boolean?): Boolean {
        return (webViewIsVisible && webViewCanGoBack == true).apply {
            goBackEvent.postUnitEvent()
        }
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

    private fun performFetchConfigurationRequest() {
        initialConnectData?.configurationUrl?.let {
            apiManager.getProviderConfigurationData(it, resultCallback = this)
        } ?: onShowErrorEvent.postValue(
            ViewModelEvent(appContext.getString(R.string.errors_unable_connect_provider))
        )
    }

    private fun performCreateConnectionRequest() {
        if (connection.connectUrl.isNotEmpty()) {
            apiManager.createConnectionRequest(
                appContext = appContext,
                connection = connection,
                pushToken = preferenceRepository.cloudMessagingToken,
                connectQueryParam = initialConnectData?.connectQuery,
                resultCallback = this
            )
        } else onShowErrorEvent.postValue(
            ViewModelEvent(appContext.getString(R.string.errors_unable_connect_provider))
        )
    }

    private fun loadWebRedirectUrl() {
        onUrlChangedEvent.postValue(ViewModelEvent(authenticateData?.redirectUrl ?: ""))
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
            val connectionName = connection.name
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
            ) ?: ""
        }
    }
}

enum class ViewMode {
    START_NEW_CONNECT, START_RECONNECT, WEB_ENROLL, COMPLETE_SUCCESS, COMPLETE_ERROR;
}
