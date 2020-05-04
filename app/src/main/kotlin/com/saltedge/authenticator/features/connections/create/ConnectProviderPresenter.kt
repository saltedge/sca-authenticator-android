/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2019 Salt Edge Inc.
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
import android.webkit.URLUtil.isValidUrl
import com.saltedge.authenticator.R
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.models.toConnection
import com.saltedge.authenticator.sdk.AuthenticatorApiManager
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.contract.ConnectionCreateListener
import com.saltedge.authenticator.sdk.contract.FetchProviderConfigurationListener
import com.saltedge.authenticator.sdk.model.*
import com.saltedge.authenticator.sdk.model.appLink.ConnectAppLinkData
import com.saltedge.authenticator.sdk.model.configuration.ProviderConfigurationData
import com.saltedge.authenticator.sdk.model.configuration.isValid
import com.saltedge.authenticator.sdk.model.connection.ConnectionStatus
import com.saltedge.authenticator.sdk.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.model.error.getErrorMessage
import com.saltedge.authenticator.sdk.model.response.CreateConnectionResponseData
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.sdk.tools.parseRedirect
import javax.inject.Inject

class ConnectProviderPresenter @Inject constructor(
    private val appContext: Context,
    private val preferenceRepository: PreferenceRepositoryAbs,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyStoreManagerAbs,
    private val apiManager: AuthenticatorApiManagerAbs
) : ConnectProviderContract.Presenter, ConnectionCreateListener, FetchProviderConfigurationListener {

    override val reportProblemActionText: Int?
        get() = if (viewMode.isCompleteWithSuccess) null else R.string.actions_contact_support
    private var sessionFailMessage: String? = null
    private var initialConnectData: ConnectAppLinkData? = null
    private var authenticateData: CreateConnectionResponseData? = null
    private var connection = Connection()
    private var viewMode: ViewMode = ViewMode.START_NEW_CONNECT
    override var viewContract: ConnectProviderContract.View? = null

    override val shouldShowProgressView: Boolean
        get() = viewMode == ViewMode.START_NEW_CONNECT || viewMode == ViewMode.START_RECONNECT
    override val shouldShowWebView: Boolean
        get() = viewMode == ViewMode.WEB_ENROLL
    override val shouldShowCompleteView: Boolean
        get() = viewMode == ViewMode.COMPLETE_SUCCESS || viewMode == ViewMode.COMPLETE_ERROR
    override val logoUrl: String
        get() = connection.logoUrl
    override val iconResId: Int
        get() = if (viewMode.isCompleteWithSuccess) R.drawable.ic_complete_ok_70 else R.drawable.ic_auth_error_70
    override val mainActionTextResId: Int
        get() = if (viewMode.isCompleteWithSuccess) R.string.actions_proceed else R.string.actions_try_again
    override val completeTitle: String
        get() {
            return if (viewMode.isCompleteWithSuccess) {
                appContext.getString(R.string.connect_status_provider_success).format(connection.name)
            } else {
                appContext.getString(R.string.errors_connection_failed)
            }
        }
    override val completeMessage: String
        get() {
            return if (viewMode.isCompleteWithSuccess) {
                appContext.getString(R.string.connect_status_provider_success_description)
            } else {
                sessionFailMessage ?: appContext.getString(
                    R.string.errors_connection_failed_description
                ) ?: ""
            }
        }

    override fun setInitialData(initialConnectData: ConnectAppLinkData?, connectionGuid: GUID?) {
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

    override fun getTitleResId(): Int {
        return if (this.connection.guid.isEmpty()) R.string.connections_new_connection
        else R.string.actions_reconnect
    }

    override fun onViewCreated() {
        when (viewMode) {
            ViewMode.START_NEW_CONNECT -> performFetchConfigurationRequest()
            ViewMode.START_RECONNECT -> performCreateConnectionRequest()
            ViewMode.WEB_ENROLL -> loadWebRedirectUrl()
            ViewMode.COMPLETE_SUCCESS -> Unit
            ViewMode.COMPLETE_ERROR -> Unit
        }
    }

    override fun onViewClick(viewId: Int) {
        if (viewId == R.id.mainActionView) viewContract?.closeView()
    }

    override fun fetchProviderConfigurationDataResult(result: ProviderConfigurationData?, error: ApiErrorData?) {
        when {
            error != null -> viewContract?.showErrorAndFinish(error.getErrorMessage(appContext))
            result != null && result.isValid() -> {
                result.toConnection()?.let {
                    this.connection = it
                    performCreateConnectionRequest()
                } ?: viewContract?.showErrorAndFinish(appContext.getString(R.string.errors_unable_connect_provider))
            }
            else -> viewContract?.showErrorAndFinish(appContext.getString(R.string.errors_unable_connect_provider))
        }
    }

    override fun onConnectionCreateFailure(error: ApiErrorData) {
        viewContract?.showErrorAndFinish(error.getErrorMessage(appContext))
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
                        webAuthFinishSuccess(connectionID, resultAccessToken)
                    },
                    error = { errorClass, errorMessage ->
                        webAuthFinishError(errorClass, errorMessage)
                    }
                )
            } else if (isValidUrl(response.redirectUrl ?: "")) {
                connection.id = response.connectionId ?: ""
                authenticateData = response
                viewMode = ViewMode.WEB_ENROLL
                viewContract?.updateViewsContent()
                loadWebRedirectUrl()
            }
        }
    }

    override fun onDestroyView() {
        if (connection.guid.isNotEmpty() && connection.accessToken.isEmpty()) {
            keyStoreManager.deleteKeyPair(connection.guid)
        }
    }

    override fun webAuthFinishError(errorClass: String, errorMessage: String?) {
        viewMode = ViewMode.COMPLETE_ERROR
        sessionFailMessage = errorMessage
        viewContract?.updateViewsContent()
    }

    override fun webAuthFinishSuccess(id: ConnectionID, accessToken: Token) {
        authFinishedWithSuccess(connectionId = id, accessToken = accessToken)
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
        viewContract?.updateViewsContent()
    }

    private fun performFetchConfigurationRequest() {
        initialConnectData?.configurationUrl?.let {
            apiManager.getProviderConfigurationData(it, resultCallback = this)
        } ?: viewContract?.showErrorAndFinish(appContext.getString(R.string.errors_unable_connect_provider))
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
            viewContract?.showErrorAndFinish(appContext.getString(R.string.errors_unable_connect_provider))
        }
    }

    private fun loadWebRedirectUrl() {
        viewContract?.loadUrlInWebView(url = authenticateData?.redirectUrl ?: "")
    }

    private val ViewMode.isCompleteWithSuccess: Boolean
        get() = this == ViewMode.COMPLETE_SUCCESS
}

enum class ViewMode {
    START_NEW_CONNECT, START_RECONNECT, WEB_ENROLL, COMPLETE_SUCCESS, COMPLETE_ERROR;
}
