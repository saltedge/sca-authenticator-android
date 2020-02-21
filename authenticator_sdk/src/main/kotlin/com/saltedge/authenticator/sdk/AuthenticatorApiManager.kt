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
import com.saltedge.authenticator.sdk.constants.DEFAULT_RETURN_URL
import com.saltedge.authenticator.sdk.contract.*
import com.saltedge.authenticator.sdk.model.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.request.ConfirmDenyData
import com.saltedge.authenticator.sdk.network.RestClient
import com.saltedge.authenticator.sdk.network.connector.*
import com.saltedge.authenticator.sdk.polling.AuthorizationsPollingService
import com.saltedge.authenticator.sdk.polling.PollingServiceAbs
import com.saltedge.authenticator.sdk.polling.SingleAuthorizationPollingService
import com.saltedge.authenticator.sdk.tools.buildUserAgent

/**
 * Wrap network communication with Identity Service
 */
object AuthenticatorApiManager : AuthenticatorApiManagerAbs {

    /**
     * Url where WebView will be redirected on enrollment finish
     */
    override var authenticationReturnUrl: String = DEFAULT_RETURN_URL
    var userAgentInfo = ""
        private set

    /**
     * Request to get Service Provide configuration.
     * Result is returned through callback.
     */
    override fun getProviderConfigurationData(
        providerConfigurationUrl: String,
        resultCallback: FetchProviderConfigurationDataResult
    ) {
        ProviderDataConnector(RestClient.apiInterface, resultCallback)
            .fetchProviderData(providerConfigurationUrl)
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
        resultCallback: ConnectionCreateResult
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
     * Request to revoke SCA connection.
     * Result is returned through callback.
     */
    override fun revokeConnections(
        connectionsAndKeys: List<ConnectionAndKey>,
        resultCallback: ConnectionsRevokeResult?
    ) {
        ConnectionsRevokeConnector(RestClient.apiInterface, resultCallback)
            .revokeTokensFor(connectionsAndKeys)
    }

    /**
     * Request to get active SCA Authorizations list.
     * Result is returned through callback.
     */
    override fun getAuthorizations(
        connectionsAndKeys: List<ConnectionAndKey>,
        resultCallback: FetchAuthorizationsResult
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
        connectionAndKey: ConnectionAndKey,
        authorizationId: String,
        resultCallback: FetchAuthorizationResult
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
        connectionAndKey: ConnectionAndKey,
        authorizationId: String,
        authorizationCode: String?,
        resultCallback: ConfirmAuthorizationResult
    ) {
        ConfirmOrDenyConnector(RestClient.apiInterface, resultCallback)
            .updateAuthorization(
                connectionAndKey = connectionAndKey,
                authorizationId = authorizationId,
                payloadData = ConfirmDenyData(
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
        connectionAndKey: ConnectionAndKey,
        authorizationId: String,
        authorizationCode: String?,
        resultCallback: ConfirmAuthorizationResult
    ) {
        ConfirmOrDenyConnector(RestClient.apiInterface, resultCallback)
            .updateAuthorization(
                connectionAndKey = connectionAndKey,
                authorizationId = authorizationId,
                payloadData = ConfirmDenyData(
                    authorizationCode = authorizationCode,
                    confirm = false
                )
            )
    }

    /**
     * Initialize SDK
     *
     * @param userAgent contain up-to-date information about the Application and the user's device
     */
    override fun initializeSDK(context: Context): String {
        userAgentInfo = buildUserAgent(context)
        return userAgentInfo
    }
}
