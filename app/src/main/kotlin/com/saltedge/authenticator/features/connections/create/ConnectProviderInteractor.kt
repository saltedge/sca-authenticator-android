/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
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

import com.saltedge.authenticator.app.ERROR_INVALID_AUTHENTICATION_DATA
import com.saltedge.authenticator.app.ERROR_INVALID_DEEPLINK
import com.saltedge.authenticator.app.ERROR_INVALID_RESPONSE
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.api.model.GUID
import com.saltedge.authenticator.sdk.api.model.connection.ConnectionStatus
import com.saltedge.authenticator.sdk.tools.isReturnToUrl
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.sdk.tools.parseRedirect
import com.saltedge.authenticator.sdk.v2.api.model.ConnectionID
import com.saltedge.authenticator.sdk.v2.api.model.Token
import com.saltedge.authenticator.sdk.v2.api.model.appLink.ConnectAppLinkDataV2
import com.saltedge.authenticator.sdk.v2.api.model.error.ApiErrorData

abstract class ConnectProviderInteractor(
    private val keyStoreManager: KeyStoreManagerAbs,
    private val preferenceRepository: PreferenceRepositoryAbs,
    private val connectionsRepository: ConnectionsRepositoryAbs,
) : ConnectProviderInteractorAbs {
    override var contract: ConnectProviderInteractorCallback? = null
    override var authenticationUrl: String = ""
    override val hasConfigUrl: Boolean
        get() = initialConnectData?.configurationUrl != null
    override val hasConnection: Boolean
        get() = connection.guid.isNotEmpty()
    override val connectionName: String
        get() = connection.name
    override val geolocationRequired: Boolean?
        get() = connection.geolocationRequired

    private var initialConnectData: ConnectAppLinkDataV2? = null
    private var connection = Connection()

    override fun setInitialData(initialConnectData: ConnectAppLinkDataV2?, connectionGuid: GUID?) {
        this.initialConnectData = initialConnectData
        this.connection = connectionsRepository.getByGuid(connectionGuid) ?: Connection()
    }

    override fun fetchScaConfiguration() {
        initialConnectData?.configurationUrl?.let {
            requestProviderConfiguration(url = it)
        } ?: contract?.onReceiveApiError(ApiErrorData(errorClassName = ERROR_INVALID_DEEPLINK))
    }

    abstract override fun requestProviderConfiguration(url: String)

    override fun setNewConnection(newConnection: Connection?) {
        newConnection?.let {
            this.connection = it
            requestCreateConnection()
        } ?: contract?.onReceiveApiError(ApiErrorData(errorClassName = ERROR_INVALID_RESPONSE))
    }

    override fun requestCreateConnection() {
        requestCreateConnection(
            connection = connection,
            cloudMessagingToken = preferenceRepository.cloudMessagingToken,
            connectQuery = initialConnectData?.connectQuery
        )
    }

    abstract override fun requestCreateConnection(connection: Connection, cloudMessagingToken: String, connectQuery: String?)

    override fun onConnectionCreateSuccess(authenticationUrl: String, connectionId: String) {
        if (authenticationUrl.isNotEmpty()) {
            if (authenticationUrl.isReturnToUrl()) {
                onReceiveReturnToUrl(authenticationUrl)
            } else {
                connection.id = connectionId
                this.authenticationUrl = authenticationUrl
                contract?.onReceiveAuthenticationUrl()
            }
        } else {
            contract?.onReceiveApiError(ApiErrorData(errorClassName = ERROR_INVALID_AUTHENTICATION_DATA))
        }
    }

    override fun onReceiveReturnToUrl(url: String) {
        parseRedirect(
            url = url,
            success = { connectionID, resultAccessToken ->
                onConnectionSuccessAuthentication(connectionID, resultAccessToken)
            },
            error = {
                errorClass, errorMessage -> contract?.onConnectionFailAuthentication(errorClass, errorMessage)
            }
        )
    }

    override fun onConnectionSuccessAuthentication(connectionId: ConnectionID, accessToken: Token) {
        connection.id = connectionId
        connection.accessToken = accessToken
        connection.status = "${ConnectionStatus.ACTIVE}"
        if (connectionsRepository.connectionExists(connection)) {
            connectionsRepository.saveModel(connection)
        } else {
            connectionsRepository.fixNameAndSave(connection)
        }
        contract?.onConnectionSuccessAuthentication()
    }

    override fun destroyConnectionIfNotAuthorized() {
        if (connection.guid.isNotEmpty() && connection.accessToken.isEmpty()) {
            keyStoreManager.deleteKeyPair(connection.guid)
        }
    }
}

interface ConnectProviderInteractorAbs {
    var contract: ConnectProviderInteractorCallback?
    var authenticationUrl: String
    val hasConfigUrl: Boolean
    val hasConnection: Boolean
    val connectionName: String
    val geolocationRequired: Boolean?

    fun setInitialData(initialConnectData: ConnectAppLinkDataV2?, connectionGuid: GUID?)
    fun fetchScaConfiguration()
    fun requestProviderConfiguration(url: String)
    fun setNewConnection(newConnection: Connection?)
    fun requestCreateConnection()
    fun requestCreateConnection(connection: Connection, cloudMessagingToken: String, connectQuery: String?)
    fun onConnectionCreateSuccess(authenticationUrl: String, connectionId: String)
    fun onReceiveReturnToUrl(url: String)
    fun onConnectionSuccessAuthentication(connectionId: ConnectionID, accessToken: Token)
    fun destroyConnectionIfNotAuthorized()
}

interface ConnectProviderInteractorCallback {
    fun onReceiveApiError(error: ApiErrorData)
    fun onReceiveAuthenticationUrl()
    fun onConnectionFailAuthentication(errorClass: String, errorMessage: String?)
    fun onConnectionSuccessAuthentication()
}
