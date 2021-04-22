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
package com.saltedge.authenticator.sdk.v2

import android.content.Context
import com.saltedge.authenticator.sdk.v2.api.RestClient
import com.saltedge.authenticator.sdk.v2.api.connector.*
import com.saltedge.authenticator.sdk.v2.api.contract.*
import com.saltedge.authenticator.sdk.v2.config.ERROR_CLASS_API_REQUEST
import com.saltedge.authenticator.sdk.v2.api.model.connection.ConnectionAbs
import com.saltedge.authenticator.sdk.v2.api.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.v2.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.v2.api.model.request.ConfirmDenyRequestData
import com.saltedge.authenticator.sdk.v2.polling.AuthorizationsPollingService
import com.saltedge.authenticator.sdk.v2.polling.PollingServiceAbs
import com.saltedge.authenticator.sdk.v2.polling.SingleAuthorizationPollingService
import com.saltedge.authenticator.sdk.v2.tools.secure.KeyStoreManager

/**
 * Wrap network communication with Salt Edge SCA Service (Authenticator API v2)
 */
class ScaServiceClient : ScaServiceClientAbs {

    /**
     * Request to get SCA Service configuration.
     * Result is returned through callback.
     */
    override fun getProviderConfigurationData(
        configurationUrl: String,
        resultCallback: FetchProviderConfigurationListener
    ) {
        ProviderConfigurationConnector(RestClient.apiInterface, resultCallback)
            .fetchProviderConfiguration(configurationUrl)
    }

    /**
     * Request to create new SCA Service connection.
     * Result is returned through callback.
     */
    override fun createConnectionRequest(
        baseUrl: String,
        dhPublicKey: String,
        encRsaPublicKey: String,
        providerId: String,
        pushToken: String,
        connectQueryParam: String?,
        resultCallback: ConnectionCreateListener
    ) {
        ConnectionInitConnector(RestClient.apiInterface, resultCallback)
            .postConnectionData(
                baseUrl = baseUrl,
                dhPublicKey = dhPublicKey,
                encRsaPublicKey = encRsaPublicKey,
                providerId = providerId,
                pushToken = pushToken,
                connectQueryParam = connectQueryParam
            )
    }

    /**
     * Request to create new SCA Service connection.
     * Result is returned through callback.
     */
    override fun createConnectionRequest(
        appContext: Context,
        connection: ConnectionAbs,
        providerId: String,
        pushToken: String,
        connectQueryParam: String?,
        resultCallback: ConnectionCreateListener
    ) {
        val publicKey = KeyStoreManager.createRsaPublicKeyAsString(appContext, connection.guid)
        //TODO: Generate DH keypair

        if (publicKey == null) {
            resultCallback.onConnectionCreateFailure(
                ApiErrorData(errorClassName = ERROR_CLASS_API_REQUEST, errorMessage = "Secure material is unavailable")
            )
        } else {
            createConnectionRequest(
                baseUrl = connection.connectUrl,
                dhPublicKey = dhPublicKey,
                encRsaPublicKey = encRsaPublicKey,
                providerId = providerId,
                pushToken = pushToken,
                connectQueryParam = connectQueryParam,
                resultCallback = resultCallback
            )
        }
    }

    /**
     * Request to revoke SCA Service connection.
     * Result is returned through callback.
     */
    override fun revokeConnections(
        connectionsAndKeys: List<ConnectionAndKey>,
        validSeconds: Int,
        resultCallback: ConnectionsRevokeListener?
    ) {
        ConnectionsRevokeConnector(RestClient.apiInterface, resultCallback)
            .revokeTokensFor(connectionsAndKeys, validSeconds)
    }

    /**
     * Request to get all active SCA Service Authorizations list.
     * Result is returned through callback.
     */
    override fun getAuthorizations(
        connectionsAndKeys: List<ConnectionAndKey>,
        resultCallback: FetchEncryptedDataListener
    ) {
        AuthorizationsConnector(RestClient.apiInterface, resultCallback)
            .fetchAuthorizations(connectionsAndKeys = connectionsAndKeys)
    }

    /**
     * Create Polling Service for an SCA Service Authorizations list status
     */
    override fun createAuthorizationsPollingService(): PollingServiceAbs<FetchAuthorizationsContract> {
        return AuthorizationsPollingService()
    }

    /**
     * Request to get active SCA Service Authorization.
     * Result is returned through callback.
     */
    override fun getAuthorization(
        connectionAndKey: ConnectionAndKey,
        authorizationId: String,
        resultCallback: FetchAuthorizationListener
    ) {
        AuthorizationConnector(RestClient.apiInterface, resultCallback)
            .getAuthorization(connectionAndKey, authorizationId)
    }

    /**
     * Create Polling Service for an SCA Service Authorization status
     */
    override fun createSingleAuthorizationPollingService(): SingleAuthorizationPollingService =
        SingleAuthorizationPollingService()

    /**
     * Request to confirm SCA Service Authorization.
     * Result is returned through callback.
     */
    override fun confirmAuthorization(
        connectionAndKey: ConnectionAndKey,
        authorizationId: String,
        authorizationCode: String?,
        geolocation: String?,
        authorizationType: String?,
        validSeconds: Int,
        payload: String,
        resultCallback: ConfirmAuthorizationListener
    ) {
        ConfirmOrDenyConnector(RestClient.apiInterface, resultCallback)
            .updateAuthorization(
                connectionAndKey = connectionAndKey,
                authorizationId = authorizationId,
                geolocationHeader = geolocation,
                authorizationTypeHeader = authorizationType,
                payloadData = ConfirmDenyRequestData(
                    confirm = true,
                    payload = payload
                ),
                validSeconds = validSeconds
            )
    }

    /**
     * Request to deny SCA Service Authorization.
     * Result is returned through callback.
     */
    override fun denyAuthorization(
        connectionAndKey: ConnectionAndKey,
        authorizationId: String,
        authorizationCode: String?,
        geolocation: String?,
        authorizationType: String?,
        validSeconds: Int,
        payload: String,
        resultCallback: ConfirmAuthorizationListener
    ) {
        ConfirmOrDenyConnector(RestClient.apiInterface, resultCallback)
            .updateAuthorization(
                connectionAndKey = connectionAndKey,
                authorizationId = authorizationId,
                geolocationHeader = geolocation,
                authorizationTypeHeader = authorizationType,
                payloadData = ConfirmDenyRequestData(
                    confirm = false,
                    payload = payload
                ),
                validSeconds = validSeconds
            )
    }
}

interface ScaServiceClientAbs {
    fun getProviderConfigurationData(
        configurationUrl: String,
        resultCallback: FetchProviderConfigurationListener
    )
    fun createConnectionRequest(
        baseUrl: String,
        dhPublicKey: String,
        encRsaPublicKey: String,
        providerId: String,
        pushToken: String,
        connectQueryParam: String?,
        resultCallback: ConnectionCreateListener
    )
    fun createConnectionRequest(
        appContext: Context,
        connection: ConnectionAbs,
//        dhPublicKey: String,
//        encRsaPublicKey: String,
        providerId: String,
        pushToken: String,
        connectQueryParam: String?,
        resultCallback: ConnectionCreateListener
    )
    fun revokeConnections(
        connectionsAndKeys: List<ConnectionAndKey>,
        validSeconds: Int,
        resultCallback: ConnectionsRevokeListener?
    )
    fun getAuthorizations(
        connectionsAndKeys: List<ConnectionAndKey>,
        resultCallback: FetchEncryptedDataListener
    )
    fun createAuthorizationsPollingService(): PollingServiceAbs<FetchAuthorizationsContract>
    fun getAuthorization(
        connectionAndKey: ConnectionAndKey,
        authorizationId: String,
        resultCallback: FetchAuthorizationListener
    )
    fun createSingleAuthorizationPollingService(): SingleAuthorizationPollingService
    fun confirmAuthorization(
        connectionAndKey: ConnectionAndKey,
        authorizationId: String,
        authorizationCode: String?,
        geolocation: String?,
        authorizationType: String?,
        validSeconds: Int,
        payload: String,
        resultCallback: ConfirmAuthorizationListener
    )
    fun denyAuthorization(
        connectionAndKey: ConnectionAndKey,
        authorizationId: String,
        authorizationCode: String?,
        geolocation: String?,
        authorizationType: String?,
        validSeconds: Int,
        payload: String,
        resultCallback: ConfirmAuthorizationListener
    )
}
