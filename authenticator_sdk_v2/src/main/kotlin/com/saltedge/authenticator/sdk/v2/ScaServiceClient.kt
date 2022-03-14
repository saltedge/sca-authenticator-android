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
import com.saltedge.authenticator.core.api.model.EncryptedBundle
import com.saltedge.authenticator.core.contract.ConsentRevokeListener
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.model.guard
import com.saltedge.authenticator.core.polling.PollingServiceAbs
import com.saltedge.authenticator.core.tools.createRandomGuid
import com.saltedge.authenticator.core.tools.json.toJsonString
import com.saltedge.authenticator.core.tools.secure.KeyAlgorithm
import com.saltedge.authenticator.core.tools.secure.KeyManager
import com.saltedge.authenticator.core.tools.secure.pemToPublicKey
import com.saltedge.authenticator.core.tools.secure.publicKeyToPem
import com.saltedge.authenticator.sdk.v2.api.connector.*
import com.saltedge.authenticator.sdk.v2.api.contract.*
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationData
import com.saltedge.authenticator.sdk.v2.api.retrofit.RestClient
import com.saltedge.authenticator.sdk.v2.polling.AuthorizationsPollingService
import com.saltedge.authenticator.sdk.v2.polling.PollingAuthorizationsContract
import com.saltedge.authenticator.sdk.v2.polling.SingleAuthorizationPollingService
import com.saltedge.authenticator.sdk.v2.tools.CryptoToolsV2

/**
 * Wrap network communication with Salt Edge SCA Service (Authenticator API v2)
 */
class ScaServiceClient : ScaServiceClientAbs {

    /**
     * Request to get SCA Service configuration.
     * Result is returned through callback.
     */
    override fun fetchProviderConfigurationData(
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
    override fun requestCreateConnection(
        baseUrl: String,
        rsaPublicKeyEncryptedBundle: EncryptedBundle,
        providerId: String,
        pushToken: String?,
        connectQueryParam: String?,
        callback: ConnectionCreateListener
    ) {
        ConnectionCreateConnector(RestClient.apiInterface, callback).postConnectionData(
            baseUrl = baseUrl,
            providerId = providerId,
            pushToken = pushToken,
            connectQueryParam = connectQueryParam,
            encryptedRsaPublicKey = rsaPublicKeyEncryptedBundle
        )
    }

    /**
     * Request to create new SCA Service connection.
     * Result is returned through callback.
     */
    override fun requestCreateConnection(
        appContext: Context,
        connection: ConnectionAbs,
        connectQueryParam: String?,
        pushToken: String?,
        callback: ConnectionCreateListener
    ) {
        val providerRsaPublicKey = connection.providerRsaPublicKeyPem.pemToPublicKey(
            algorithm = KeyAlgorithm.RSA
        ).guard {
            callback.error("RSA secure material of provider is invalid")
            return
        }
        if (connection.guid.isEmpty()) {
            connection.guid = createRandomGuid()
        }
        val appRsaPublicKey = KeyManager.createOrReplaceRsaKeyPair(
            context = appContext,
            alias = connection.guid
        )?.public.guard {
            callback.error("RSA secure material is unavailable")
            return
        }
        val rsaPublicKeyEncryptedBundle = CryptoToolsV2.createEncryptedBundle(
            payload = appRsaPublicKey.publicKeyToPem(),
            rsaPublicKey = providerRsaPublicKey
        ).guard {
            callback.error("User data encryption failed")
            return
        }
        requestCreateConnection(
            baseUrl = connection.connectUrl,
            rsaPublicKeyEncryptedBundle = rsaPublicKeyEncryptedBundle,
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
    override fun revokeConnections(
        richConnections: List<RichConnection>,
        callback: ConnectionsV2RevokeListener?
    ) {
        ConnectionsRevokeConnector(RestClient.apiInterface, callback)
            .revokeAccess(forConnections = richConnections)
    }

    /**
     * Request to get all active SCA Service Authorizations list.
     * Result is returned through callback.
     */
    override fun fetchAuthorizations(
        richConnections: List<RichConnection>,
        callback: FetchAuthorizationsListener
    ) {
        AuthorizationsIndexConnector(RestClient.apiInterface, callback)
            .fetchActiveAuthorizations(connections = richConnections)
    }

    /**
     * Create Polling Service for an SCA Service Authorizations list status
     */
    override fun createAuthorizationsPollingService(): PollingServiceAbs<PollingAuthorizationsContract> {
        return AuthorizationsPollingService()
    }

    /**
     * Request to get active SCA Service Authorization.
     * Result is returned through callback.
     */
    override fun fetchAuthorization(
        richConnection: RichConnection,
        authorizationID: ID,
        callback: FetchAuthorizationListener
    ) {
        AuthorizationShowConnector(RestClient.apiInterface, callback)
            .showAuthorization(richConnection.connection, authorizationID)
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
        richConnection: RichConnection,
        authorizationID: ID,
        authorizationData: UpdateAuthorizationData,
        callback: AuthorizationConfirmListener
    ) {
        val encryptedPayload = CryptoToolsV2.createEncryptedBundle(
            payload = authorizationData.toJsonString(),
            rsaPublicKey = richConnection.providerPublic
        ).guard {
            callback.error(
                message = "User data encryption failed",
                authorizationID = authorizationID,
                connectionID = richConnection.connection.id
            )
            return
        }
        AuthorizationConfirmConnector(
            apiInterface = RestClient.apiInterface,
            authorizationId = authorizationID,
            callback = callback
        ).confirmAuthorization(connection = richConnection, encryptedPayload = encryptedPayload)
    }

    /**
     * Request to deny SCA Service Authorization.
     * Result is returned through callback.
     */
    override fun denyAuthorization(
        richConnection: RichConnection,
        authorizationID: ID,
        authorizationData: UpdateAuthorizationData,
        callback: AuthorizationDenyListener
    ) {
        val encryptedPayload = CryptoToolsV2.createEncryptedBundle(
            payload = authorizationData.toJsonString(),
            rsaPublicKey = richConnection.providerPublic
        ).guard {
            callback.error(
                message = "User data encryption failed",
                authorizationID = authorizationID,
                connectionID = richConnection.connection.id
            )
            return
        }
        AuthorizationDenyConnector(
            apiInterface = RestClient.apiInterface,
            authorizationId = authorizationID,
            callback = callback
        ).denyAuthorization(richConnection = richConnection, encryptedPayload = encryptedPayload)
    }

    override fun requestCreateAuthorizationForAction(
        richConnection: RichConnection,
        actionID: ID,
        callback: AuthorizationCreateListener
    ) {
        AuthorizationCreateConnector(apiInterface = RestClient.apiInterface, callback = callback)
            .createAuthorizationForAction(richConnection = richConnection, actionID = actionID)
    }

    /**
     * Request to get all active SCA Service Consents list.
     * Result is returned through callback.
     */
    override fun fetchConsents(
        richConnections: List<RichConnection>,
        callback: FetchConsentsListener
    ) {
        ConsentsIndexConnector(RestClient.apiInterface, callback)
            .fetchActiveConsents(connections = richConnections)
    }

    /**
     * Request to revoke SCA Service Consent.
     * Result is returned through callback.
     */
    override fun revokeConsent(
        consentID: ID,
        richConnection: RichConnection,
        callback: ConsentRevokeListener?
    ) {
        ConsentRevokeConnector(RestClient.apiInterface, callback).revokeConsent(consentID = consentID, richConnection = richConnection)
    }
}

interface ScaServiceClientAbs {
    fun fetchProviderConfigurationData(configurationUrl: String, callback: FetchConfigurationListener)
    fun requestCreateConnection(
        baseUrl: String,
        rsaPublicKeyEncryptedBundle: EncryptedBundle,
        providerId: String,
        pushToken: String?,
        connectQueryParam: String?,
        callback: ConnectionCreateListener
    )
    fun requestCreateConnection(
        appContext: Context,
        connection: ConnectionAbs,
        connectQueryParam: String?,
        pushToken: String?,
        callback: ConnectionCreateListener
    )
    fun revokeConnections(richConnections: List<RichConnection>, callback: ConnectionsV2RevokeListener?)
    fun fetchAuthorizations(richConnections: List<RichConnection>, callback: FetchAuthorizationsListener)
    fun createAuthorizationsPollingService(): PollingServiceAbs<PollingAuthorizationsContract>
    fun fetchAuthorization(richConnection: RichConnection, authorizationID: ID, callback: FetchAuthorizationListener)
    fun createSingleAuthorizationPollingService(): SingleAuthorizationPollingService
    fun confirmAuthorization(
        richConnection: RichConnection,
        authorizationID: ID,
        authorizationData: UpdateAuthorizationData,
        callback: AuthorizationConfirmListener
    )
    fun denyAuthorization(
        richConnection: RichConnection,
        authorizationID: ID,
        authorizationData: UpdateAuthorizationData,
        callback: AuthorizationDenyListener
    )
    fun requestCreateAuthorizationForAction(
        richConnection: RichConnection,
        actionID: ID,
        callback: AuthorizationCreateListener
    )
    fun fetchConsents(
        richConnections: List<RichConnection>,
        callback: FetchConsentsListener
    )
    fun revokeConsent(
        consentID: ID,
        richConnection: RichConnection,
        callback: ConsentRevokeListener?
    )
}
