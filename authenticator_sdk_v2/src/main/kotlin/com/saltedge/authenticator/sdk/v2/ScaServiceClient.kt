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
import com.saltedge.authenticator.sdk.v2.api.connector.*
import com.saltedge.authenticator.sdk.v2.api.contract.*
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationData
import com.saltedge.authenticator.sdk.v2.api.model.connection.ConnectionV2Abs
import com.saltedge.authenticator.sdk.v2.api.model.connection.RichConnection
import com.saltedge.authenticator.sdk.v2.api.model.guard
import com.saltedge.authenticator.sdk.v2.api.retrofit.RestClient
import com.saltedge.authenticator.sdk.v2.polling.AuthorizationsPollingService
import com.saltedge.authenticator.sdk.v2.polling.PollingServiceAbs
import com.saltedge.authenticator.sdk.v2.polling.SingleAuthorizationPollingService
import com.saltedge.authenticator.sdk.v2.tools.createRandomGuid
import com.saltedge.authenticator.sdk.v2.tools.json.toJsonString
import com.saltedge.authenticator.sdk.v2.tools.secure.*

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
        callback: FetchConfigurationListener
    ) {
        ConfigurationConnector(RestClient.apiInterface, callback)
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
        pushToken: String?,
        connectQueryParam: String?,
        callback: ConnectionCreateListener
    ) {
        ConnectionCreateConnector(RestClient.apiInterface, callback).postConnectionData(
            baseUrl = baseUrl,
            appDhPublicKey = dhPublicKey,
            encryptedAppRsaPublicKey = encRsaPublicKey,
            providerId = providerId,
            pushToken = pushToken,
            connectQueryParam = connectQueryParam
        )
    }

    /**
     * Request to create new SCA Service connection.
     * Result is returned through callback.
     */
    override fun initConnectionRequest(
        appContext: Context,
        connection: ConnectionV2Abs,
        connectQueryParam: String?,
        pushToken: String?,
        callback: ConnectionCreateListener
    ) {
        val providerDhPublicKey = convertPemToPublicKey(
            pem = connection.providerDhPublicKey,
            algorithm = KeyAlgorithm.DIFFIE_HELLMAN
        ).guard {
            callback.error("Diffie-Hellman secure material of provider is invalid")
            return
        }
        val dhAlias = createRandomGuid()
        val dhKeyPair = KeyManager.createOrReplaceDhKeyPair(
            context = appContext,
            alias = dhAlias,
            outDhPublicKey = providerDhPublicKey
        ).guard {
            callback.error("Diffie-Hellman secure material is unavailable")
            return
        }
        val rsaAlias = createRandomGuid()
        val publicKeyPem = KeyManager.createOrReplaceRsaKeyPair(
            context = appContext,
            alias = rsaAlias
        )?.publicKeyToPemString().guard {
            callback.error("RSA secure material is unavailable")
            return
        }

        val sharedSecret = KeyTools.computeSecretKey(
            privateDhKey = dhKeyPair.private,
            publicDhKey = providerDhPublicKey
        )
        connection.appDhKeyAlias = dhAlias
        connection.guid = rsaAlias
        createConnectionRequest(
            baseUrl = connection.connectUrl,
            dhPublicKey = dhKeyPair.publicKeyToPemString(),
            encRsaPublicKey = CryptoTools.aesEncrypt(data = publicKeyPem, key = sharedSecret),
            providerId = connection.code,
            pushToken = pushToken,
            connectQueryParam = connectQueryParam,
            callback = callback
        )
    }

    /**
     * Request to revoke SCA Service connection.
     * Result is returned through callback.
     */
    override fun revokeConnections(connections: List<RichConnection>, callback: ConnectionsRevokeListener?) {
        ConnectionsRevokeConnector(RestClient.apiInterface, callback)
            .revokeTokensFor(connections = connections)
    }

    /**
     * Request to get all active SCA Service Authorizations list.
     * Result is returned through callback.
     */
    override fun getAuthorizations(connections: List<RichConnection>, callback: FetchAuthorizationsListener) {
        AuthorizationsIndexConnector(RestClient.apiInterface, callback)
            .fetchActiveAuthorizations(connections = connections)
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
        connection: RichConnection,
        authorizationId: String,
        callback: FetchAuthorizationListener
    ) {
        AuthorizationShowConnector(RestClient.apiInterface, callback)
            .showAuthorization(connection.connection, authorizationId)
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
        connection: RichConnection,
        authorizationId: String,
        authorizationData: UpdateAuthorizationData,
        callback: AuthorizationConfirmListener
    ) {
        val encryptedPayload = CryptoTools.aesEncrypt(
            authorizationData.toJsonString(),
            connection.aesSharedSecret
        )
        AuthorizationConfirmConnector(
            apiInterface = RestClient.apiInterface,
            authorizationId = authorizationId,
            callback = callback
        ).confirmAuthorization(richConnection = connection, encryptedPayload = encryptedPayload)
    }

    /**
     * Request to deny SCA Service Authorization.
     * Result is returned through callback.
     */
    override fun denyAuthorization(
        connection: RichConnection,
        authorizationId: String,
        authorizationData: UpdateAuthorizationData,
        callback: AuthorizationDenyListener
    ) {
        val encryptedPayload = CryptoTools.aesEncrypt(
            authorizationData.toJsonString(),
            connection.aesSharedSecret
        )
        AuthorizationDenyConnector(
            apiInterface = RestClient.apiInterface,
            authorizationId = authorizationId,
            callback = callback
        ).denyAuthorization(richConnection = connection, encryptedPayload = encryptedPayload)
    }
}

interface ScaServiceClientAbs {
    fun getProviderConfigurationData(configurationUrl: String, callback: FetchConfigurationListener)
    fun createConnectionRequest(
        baseUrl: String,
        dhPublicKey: String,
        encRsaPublicKey: String,
        providerId: String,
        pushToken: String?,
        connectQueryParam: String?,
        callback: ConnectionCreateListener
    )
    fun initConnectionRequest(
        appContext: Context,
        connection: ConnectionV2Abs,
        connectQueryParam: String?,
        pushToken: String?,
        callback: ConnectionCreateListener
    )
    fun revokeConnections(connections: List<RichConnection>, callback: ConnectionsRevokeListener?)
    fun getAuthorizations(connections: List<RichConnection>, callback: FetchAuthorizationsListener)
    fun createAuthorizationsPollingService(): PollingServiceAbs<FetchAuthorizationsContract>
    fun getAuthorization(
        connection: RichConnection,
        authorizationId: String,
        callback: FetchAuthorizationListener
    )
    fun createSingleAuthorizationPollingService(): SingleAuthorizationPollingService
    fun confirmAuthorization(
        connection: RichConnection,
        authorizationId: String,
        authorizationData: UpdateAuthorizationData,
        callback: AuthorizationConfirmListener
    )
    fun denyAuthorization(
        connection: RichConnection,
        authorizationId: String,
        authorizationData: UpdateAuthorizationData,
        callback: AuthorizationDenyListener
    )
}
