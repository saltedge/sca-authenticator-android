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
import com.saltedge.authenticator.sdk.polling.PollingServiceAbs
import com.saltedge.authenticator.sdk.polling.SingleAuthorizationPollingService

/**
 * Abstraction of AuthenticatorApiManager
 * @see AuthenticatorApiManager
 */
interface AuthenticatorApiManagerAbs {

    fun getProviderData(providerConfigurationUrl: String, resultCallback: FetchProviderDataResult)

    fun initConnectionRequest(
        baseUrl: String,
        publicKey: String,
        pushToken: String,
        resultCallback: ConnectionInitResult
    )

    fun revokeConnections(
        connectionsAndKeys: List<ConnectionAndKey>,
        resultCallback: ConnectionsRevokeResult?
    )

    fun getAuthorizations(
        connectionsAndKeys: List<ConnectionAndKey>,
        resultCallback: FetchAuthorizationsResult
    )

    fun createAuthorizationsPollingService(): PollingServiceAbs<FetchAuthorizationsContract>

    fun getAuthorization(
        connectionAndKey: ConnectionAndKey,
        authorizationId: String,
        resultCallback: FetchAuthorizationResult
    )

    fun createSingleAuthorizationPollingService(): SingleAuthorizationPollingService

    fun confirmAuthorization(
        connectionAndKey: ConnectionAndKey,
        authorizationId: String,
        authorizationCode: String?,
        resultCallback: ConfirmAuthorizationResult
    )

    fun denyAuthorization(
        connectionAndKey: ConnectionAndKey,
        authorizationId: String,
        authorizationCode: String?,
        resultCallback: ConfirmAuthorizationResult
    )
}
