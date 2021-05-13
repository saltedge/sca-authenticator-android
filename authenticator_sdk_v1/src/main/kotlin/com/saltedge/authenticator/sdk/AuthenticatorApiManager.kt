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
package com.saltedge.authenticator.sdk

import android.content.Context
import com.saltedge.authenticator.core.api.ERROR_CLASS_API_REQUEST
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.model.guard
import com.saltedge.authenticator.core.polling.PollingServiceAbs
import com.saltedge.authenticator.core.tools.secure.KeyManager
import com.saltedge.authenticator.core.tools.secure.publicKeyToPem
import com.saltedge.authenticator.sdk.api.RestClient
import com.saltedge.authenticator.sdk.api.connector.*
import com.saltedge.authenticator.sdk.api.model.request.ConfirmDenyRequestData
import com.saltedge.authenticator.sdk.config.ApiV1Config
import com.saltedge.authenticator.sdk.contract.*
import com.saltedge.authenticator.sdk.polling.AuthorizationsPollingService
import com.saltedge.authenticator.sdk.polling.FetchAuthorizationsContract
import com.saltedge.authenticator.sdk.polling.SingleAuthorizationPollingService

interface AuthenticatorApiManagerAbs {
    fun initializeSDK(context: Context)
    fun getProviderConfigurationData(
        providerConfigurationUrl: String,
        resultCallback: FetchProviderConfigurationListener
    )
    fun createConnectionRequest(
        baseUrl: String,
        publicKey: String,
        pushToken: String,
        providerCode: String,
        connectQueryParam: String?,
        resultCallback: ConnectionCreateListener
    )
    fun createConnectionRequest(
        appContext: Context,
        connection: ConnectionAbs,
        pushToken: String,
        connectQueryParam: String?,
        resultCallback: ConnectionCreateListener
    )
    fun revokeConnections(
        connectionsAndKeys: List<RichConnection>,
        resultCallback: ConnectionsRevokeListener?
    )
    fun getAuthorizations(
        connectionsAndKeys: List<RichConnection>,
        resultCallback: FetchEncryptedDataListener
    )
    fun createAuthorizationsPollingService(): PollingServiceAbs<FetchAuthorizationsContract>
    fun getAuthorization(
        connectionAndKey: RichConnection,
        authorizationId: String,
        resultCallback: FetchAuthorizationListener
    )
    fun createSingleAuthorizationPollingService(): SingleAuthorizationPollingService
    fun confirmAuthorization(
        connectionAndKey: RichConnection,
        authorizationId: String,
        authorizationCode: String?,
        geolocation: String?,
        authorizationType: String?,
        resultCallback: ConfirmAuthorizationListener
    )
    fun denyAuthorization(
        connectionAndKey: RichConnection,
        authorizationId: String,
        authorizationCode: String?,
        geolocation: String?,
        authorizationType: String?,
        resultCallback: ConfirmAuthorizationListener
    )
    fun sendAction(
        actionUUID: String,
        connectionAndKey: RichConnection,
        resultCallback: ActionSubmitListener
    )
    fun getConsents(
        connectionsAndKeys: List<RichConnection>,
        resultCallback: FetchEncryptedDataListener
    )
    fun revokeConsent(
        consentId: String,
        connectionAndKey: RichConnection,
        resultCallback: ConsentRevokeListener
    )
}

/**
 * Wrap network communication with Identity Service
 */
object AuthenticatorApiManager : AuthenticatorApiManagerAbs {

    /**
     * Initialize SDK
     *
     * @param context of Application
     */
    override fun initializeSDK(context: Context) {
        ApiV1Config.setupConfig(context)
    }

    /**
     * Request to get Service Provide configuration.
     * Result is returned through callback.
     */
    override fun getProviderConfigurationData(
        providerConfigurationUrl: String,
        resultCallback: FetchProviderConfigurationListener
    ) {
        ProviderConfigurationConnector(RestClient.apiInterface, resultCallback)
            .fetchProviderConfiguration(providerConfigurationUrl)
    }

    /**
     * Request to create new SCA connection.
     * Result is returned through callback.
     */
    override fun createConnectionRequest(
        baseUrl: String,
        publicKey: String,
        pushToken: String,
        providerCode: String,
        connectQueryParam: String?,
        resultCallback: ConnectionCreateListener
    ) {
        ConnectionInitConnector(RestClient.apiInterface, resultCallback)
            .postConnectionData(
                baseUrl = baseUrl,
                publicKey = publicKey,
                pushToken = pushToken,
                providerCode = providerCode,
                connectQueryParam = connectQueryParam
            )
    }

    /**
     * Request to create new SCA connection.
     * Result is returned through callback.
     */
    override fun createConnectionRequest(
        appContext: Context,
        connection: ConnectionAbs,
        pushToken: String,
        connectQueryParam: String?,
        resultCallback: ConnectionCreateListener
    ) {
        val appRsaPublicKey = KeyManager.createOrReplaceRsaKeyPair(
            context = appContext,
            alias = connection.guid
        )?.public.guard {
            resultCallback.onConnectionCreateFailure(
                ApiErrorData(errorClassName = ERROR_CLASS_API_REQUEST, errorMessage = "Secure material is unavailable")
            )
            return
        }
        createConnectionRequest(
            baseUrl = connection.connectUrl,
            publicKey = appRsaPublicKey.publicKeyToPem(),
            pushToken = pushToken,
            providerCode = connection.code,
            connectQueryParam = connectQueryParam,
            resultCallback = resultCallback
        )
    }

    /**
     * Request to revoke SCA connection.
     * Result is returned through callback.
     */
    override fun revokeConnections(
        connectionsAndKeys: List<RichConnection>,
        resultCallback: ConnectionsRevokeListener?
    ) {
        ConnectionsRevokeConnector(RestClient.apiInterface, resultCallback)
            .revokeTokensFor(connectionsAndKeys)
    }

    /**
     * Request to get active SCA Authorizations list.
     * Result is returned through callback.
     */
    override fun getAuthorizations(
        connectionsAndKeys: List<RichConnection>,
        resultCallback: FetchEncryptedDataListener
    ) {
        AuthorizationsConnector(RestClient.apiInterface, resultCallback)
            .fetchAuthorizations(connectionsAndKeys = connectionsAndKeys)
    }

    /**
     * Create Polling Service for an SCA Authorizations list status
     */
    override fun createAuthorizationsPollingService(): PollingServiceAbs<FetchAuthorizationsContract> {
        return AuthorizationsPollingService()
    }

    /**
     * Request to get active SCA Authorization.
     * Result is returned through callback.
     */
    override fun getAuthorization(
        connectionAndKey: RichConnection,
        authorizationId: String,
        resultCallback: FetchAuthorizationListener
    ) {
        AuthorizationConnector(RestClient.apiInterface, resultCallback)
            .getAuthorization(connectionAndKey, authorizationId)
    }

    /**
     * Create Polling Service for an SCA Authorization status
     */
    override fun createSingleAuthorizationPollingService(): SingleAuthorizationPollingService =
        SingleAuthorizationPollingService()

    /**
     * Request to confirm SCA Authorization.
     * Result is returned through callback.
     */
    override fun confirmAuthorization(
        connectionAndKey: RichConnection,
        authorizationId: String,
        authorizationCode: String?,
        geolocation: String?,
        authorizationType: String?,
        resultCallback: ConfirmAuthorizationListener
    ) {
        ConfirmOrDenyConnector(RestClient.apiInterface, resultCallback)
            .updateAuthorization(
                connectionAndKey = connectionAndKey,
                authorizationId = authorizationId,
                geolocationHeader = geolocation,
                authorizationTypeHeader = authorizationType,
                payloadData = ConfirmDenyRequestData(
                    authorizationCode = authorizationCode,
                    confirm = true
                )
            )
    }

    /**
     * Request to deny SCA Authorization.
     * Result is returned through callback.
     */
    override fun denyAuthorization(
        connectionAndKey: RichConnection,
        authorizationId: String,
        authorizationCode: String?,
        geolocation: String?,
        authorizationType: String?,
        resultCallback: ConfirmAuthorizationListener
    ) {
        ConfirmOrDenyConnector(RestClient.apiInterface, resultCallback)
            .updateAuthorization(
                connectionAndKey = connectionAndKey,
                authorizationId = authorizationId,
                geolocationHeader = geolocation,
                authorizationTypeHeader = authorizationType,
                payloadData = ConfirmDenyRequestData(
                    authorizationCode = authorizationCode,
                    confirm = false
                )
            )
    }

    /**
     * Request to send action.
     * Result is returned through callback.
     */
    override fun sendAction(
        actionUUID: String,
        connectionAndKey: RichConnection,
        resultCallback: ActionSubmitListener
    ) {
        SubmitActionConnector(RestClient.apiInterface, resultCallback)
            .updateAction(
                actionUUID = actionUUID,
                connectionAndKey = connectionAndKey
            )
    }

    /**
     * Request to get active User Consents list.
     * Result is returned via callback.
     */
    override fun getConsents(
        connectionsAndKeys: List<RichConnection>,
        resultCallback: FetchEncryptedDataListener
    ) {
        ConsentsConnector(RestClient.apiInterface, connectionsAndKeys, resultCallback).fetchConsents()
    }

    /**
     * Request to revoke consent
     */
    override fun revokeConsent(
        consentId: String,
        connectionAndKey: RichConnection,
        resultCallback: ConsentRevokeListener
    ) {
        ConsentRevokeConnector(RestClient.apiInterface, resultCallback).revokeConsent(consentId, connectionAndKey)
    }
}
