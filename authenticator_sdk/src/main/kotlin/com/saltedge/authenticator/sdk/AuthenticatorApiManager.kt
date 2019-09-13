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

import com.saltedge.authenticator.sdk.contract.*
import com.saltedge.authenticator.sdk.model.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.request.ConfirmDenyData
import com.saltedge.authenticator.sdk.network.RestClient
import com.saltedge.authenticator.sdk.network.connector.*
import com.saltedge.authenticator.sdk.polling.AuthorizationsPollingService
import com.saltedge.authenticator.sdk.polling.PollingServiceAbs
import com.saltedge.authenticator.sdk.polling.SingleAuthorizationPollingService

/**
 * Wrap network communication with Identity Service
 */
object AuthenticatorApiManager : AuthenticatorApiManagerAbs {

    override fun getProviderData(
        providerConfigurationUrl: String,
        resultCallback: FetchProviderDataResult
    ) {
        ProviderDataConnector(RestClient.apiInterface, resultCallback)
            .fetchProviderData(providerConfigurationUrl)
    }

    override fun initConnectionRequest(
        baseUrl: String,
        publicKey: String,
        pushToken: String,
        resultCallback: ConnectionInitResult
    ) {
        ConnectionInitConnector(RestClient.apiInterface, resultCallback)
            .postConnectionData(baseUrl, publicKey, pushToken)
    }

    override fun revokeConnections(
        connectionsAndKeys: List<ConnectionAndKey>,
        resultCallback: ConnectionsRevokeResult?
    ) {
        ConnectionsRevokeConnector(RestClient.apiInterface, resultCallback)
            .revokeTokensFor(connectionsAndKeys)
    }

    override fun getAuthorizations(
        connectionsAndKeys: List<ConnectionAndKey>,
        resultCallback: FetchAuthorizationsResult
    ) {
        AuthorizationsConnector(RestClient.apiInterface, resultCallback)
            .fetchAuthorizations(connectionsAndKeys = connectionsAndKeys)
    }

    override fun createAuthorizationsPollingService(): PollingServiceAbs<FetchAuthorizationsContract> {
        return AuthorizationsPollingService()
    }

    override fun getAuthorization(
        connectionAndKey: ConnectionAndKey,
        authorizationId: String,
        resultCallback: FetchAuthorizationResult
    ) {
        AuthorizationConnector(RestClient.apiInterface, resultCallback)
            .getAuthorization(connectionAndKey, authorizationId)
    }

    override fun createSingleAuthorizationPollingService(): SingleAuthorizationPollingService =
        SingleAuthorizationPollingService()

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
}
