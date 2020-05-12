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
import android.view.View
import android.webkit.URLUtil
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
import com.saltedge.authenticator.sdk.tools.crypt.CryptoToolsAbs
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.sdk.tools.parseRedirect
import javax.inject.Inject

class ConnectProviderViewModel @Inject constructor(
    private val appContext: Context,
    private val preferenceRepository: PreferenceRepositoryAbs,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyStoreManagerAbs,
    private val apiManager: AuthenticatorApiManagerAbs
) : ViewModel(), LifecycleObserver, ConnectionCreateListener, FetchProviderConfigurationListener {

    val iconResId: MutableLiveData<Int> = MutableLiveData(R.drawable.ic_status_success)
    val completeTitle: MutableLiveData<String> = MutableLiveData("")
    val completeDescription: MutableLiveData<String> = MutableLiveData("")
    val mainActionTextResId: MutableLiveData<Int> = MutableLiveData(R.string.actions_proceed)
    val reportProblemActionText: MutableLiveData<Int?> = MutableLiveData(null)
    val shouldShowWebViewVisibility = MutableLiveData<Int>()
    val shouldShowProgressViewVisibility = MutableLiveData<Int>()
    val shouldShowCompleteViewVisibility = MutableLiveData<Int>()

    private var connection = Connection()
    private var initialConnectData: ConnectAppLinkData? = null
    private var authenticateData: CreateConnectionResponseData? = null
    private var sessionFailMessage: String? = null
    private var viewMode: ViewMode = ViewMode.START_NEW_CONNECT

    var onCloseEvent = MutableLiveData<ViewModelEvent<Unit>>()
        private set
    var showErrorAndFinishEvent = MutableLiveData<ViewModelEvent<String>>()
        private set
    var loadUrlInWebViewEvent = MutableLiveData<ViewModelEvent<String?>>()
        private set

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
                        webAuthFinishSuccess(connectionID, resultAccessToken)
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
        showErrorAndFinishEvent.postValue(ViewModelEvent(error.getErrorMessage(appContext)))
    }

    override fun fetchProviderConfigurationDataResult(
        result: ProviderConfigurationData?,
        error: ApiErrorData?
    ) {
        when {
            error != null -> showErrorAndFinishEvent.postValue(
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
                    ?: showErrorAndFinishEvent.postValue(ViewModelEvent(appContext.getString(R.string.errors_unable_connect_provider)))
            }
            else -> showErrorAndFinishEvent.postValue(ViewModelEvent(appContext.getString(R.string.errors_unable_connect_provider)))
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        if (connection.guid.isNotEmpty() && connection.accessToken.isEmpty()) {
            keyStoreManager.deleteKeyPair(connection.guid)
        }
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
    }

    fun getTitleResId(): Int {
        return if (this.connection.guid.isEmpty()) R.string.connections_new_connection
        else R.string.actions_reconnect
    }

    fun onViewCreated() {
        when (viewMode) {
            ViewMode.START_NEW_CONNECT -> performFetchConfigurationRequest()
            ViewMode.START_RECONNECT -> performCreateConnectionRequest()
            ViewMode.WEB_ENROLL -> loadWebRedirectUrl()
            ViewMode.COMPLETE_SUCCESS -> Unit
            ViewMode.COMPLETE_ERROR -> Unit
        }
        updateViewsContent()
    }

    fun onViewClick(viewId: Int) {
        if (viewId == R.id.mainActionView) onCloseEvent.postValue(ViewModelEvent(Unit))
    }

    fun onDialogActionIdClick(dialogActionId: Int) {
        if (dialogActionId == DialogInterface.BUTTON_POSITIVE) onCloseEvent.postValue(ViewModelEvent(Unit))
    }

    fun webAuthFinishSuccess(id: ConnectionID, accessToken: Token) {
        authFinishedWithSuccess(connectionId = id, accessToken = accessToken)
    }

    fun webAuthFinishError(errorClass: String, errorMessage: String?) {
        viewMode = ViewMode.COMPLETE_ERROR
        sessionFailMessage = errorMessage
        updateViewsContent()
    }

    fun shouldShowWebView(): Boolean {
        return viewMode == ViewMode.WEB_ENROLL
    }

    private fun performFetchConfigurationRequest() {
        initialConnectData?.configurationUrl?.let {
            apiManager.getProviderConfigurationData(it, resultCallback = this)
        }
            ?: showErrorAndFinishEvent.postValue(ViewModelEvent(appContext.getString(R.string.errors_unable_connect_provider)))
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
        } else {
            showErrorAndFinishEvent.postValue(ViewModelEvent(appContext.getString(R.string.errors_unable_connect_provider)))
        }
    }

    private fun loadWebRedirectUrl() {
        loadUrlInWebViewEvent.postValue(ViewModelEvent(authenticateData?.redirectUrl ?: ""))
    }

    private fun authFinishedWithSuccess(connectionId: ConnectionID, accessToken: Token) {
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

    private val ViewMode.isCompleteWithSuccess: Boolean
        get() = this == ViewMode.COMPLETE_SUCCESS

    private fun updateViewsContent() {
        iconResId.postValue(getIconResId())
        completeTitle.postValue(getCompleteTitle())
        completeDescription.postValue(getCompleteMessage())
        mainActionTextResId.postValue(getActionTextResId())
        reportProblemActionText.postValue(getReportProblemActionText())

        shouldShowWebViewVisibility.postValue(showWebView())
        shouldShowProgressViewVisibility.postValue(showProgressView())
        shouldShowCompleteViewVisibility.postValue(showCompleteViewVisibility())
    }

    private fun getIconResId(): Int {
        return if (viewMode.isCompleteWithSuccess) R.drawable.ic_status_success else R.drawable.ic_status_error
    }

    private fun getCompleteTitle(): String {
        return if (viewMode.isCompleteWithSuccess) {
            appContext.getString(R.string.connect_status_provider_success).format(connection.name)
        } else {
            appContext.getString(R.string.errors_connection_failed)
        }
    }

    private fun getCompleteMessage(): String {
        return if (viewMode.isCompleteWithSuccess) {
            appContext.getString(R.string.connect_status_provider_success_description)
        } else {
            sessionFailMessage ?: appContext.getString(
                R.string.errors_connection_failed_description
            ) ?: ""
        }
    }

    private fun getActionTextResId(): Int {
        return if (viewMode.isCompleteWithSuccess) R.string.actions_proceed else R.string.actions_try_again
    }

    private fun getReportProblemActionText(): Int? {
        return if (viewMode.isCompleteWithSuccess) null else R.string.actions_contact_support
    }

    private fun showWebView(): Int? {
        return if (viewMode == ViewMode.WEB_ENROLL) View.VISIBLE else View.GONE
    }

    private fun showProgressView(): Int? {
        return if (viewMode == ViewMode.START_NEW_CONNECT || viewMode == ViewMode.START_RECONNECT) View.VISIBLE else View.GONE
    }

    private fun showCompleteViewVisibility(): Int? {
        return if (viewMode == ViewMode.COMPLETE_SUCCESS || viewMode == ViewMode.COMPLETE_ERROR) View.VISIBLE else View.GONE
    }
}

enum class ViewMode {
    START_NEW_CONNECT, START_RECONNECT, WEB_ENROLL, COMPLETE_SUCCESS, COMPLETE_ERROR;
}
