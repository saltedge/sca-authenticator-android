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
import com.saltedge.authenticator.sdk.contract.*
import com.saltedge.authenticator.sdk.model.connection.ConnectionAbs
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.polling.PollingServiceAbs
import com.saltedge.authenticator.sdk.polling.SingleAuthorizationPollingService

/**
 * Abstraction of AuthenticatorApiManager
 * @see AuthenticatorApiManager
 */
interface AuthenticatorApiManagerAbs {

    /**
     * Url where WebView will be redirected on enrollment finish
     */
    var authenticationReturnUrl: String

    /**
     * Initialize SDK
     *
     * @param context of Application
     */
    fun initializeSDK(context: Context)

    /**
     * Request to get Service Provide configuration.
     * Result is returned via callback.
     */
    fun getProviderConfigurationData(
        providerConfigurationUrl: String,
        resultCallback: FetchProviderConfigurationListener
    )

    /**
     * Request to create new SCA connection.
     * Result is returned via callback.
     */
    fun createConnectionRequest(
        baseUrl: String,
        publicKey: String,
        pushToken: String,
        providerCode: String,
        connectQueryParam: String?,
        resultCallback: ConnectionCreateListener
    )

    /**
     * Request to create new SCA connection.
     * Result is returned via callback.
     */
    fun createConnectionRequest(
        appContext: Context,
        connection: ConnectionAbs,
        pushToken: String,
        connectQueryParam: String?,
        resultCallback: ConnectionCreateListener
    )

    /**
     * Request to revoke SCA connection.
     * Result is returned via callback.
     */
    fun revokeConnections(
        connectionsAndKeys: List<ConnectionAndKey>,
        resultCallback: ConnectionsRevokeListener?
    )

    /**
     * Request to get active SCA Authorizations list.
     * Result is returned via callback.
     */
    fun getAuthorizations(
        connectionsAndKeys: List<ConnectionAndKey>,
        resultCallback: FetchEncryptedDataListener
    )

    /**
     * Create Polling Service for an SCA Authorizations list status
     */
    fun createAuthorizationsPollingService(): PollingServiceAbs<FetchAuthorizationsContract>

    /**
     * Request to get active SCA Authorization.
     * Result is returned via callback.
     */
    fun getAuthorization(
        connectionAndKey: ConnectionAndKey,
        authorizationId: String,
        resultCallback: FetchAuthorizationListener
    )

    /**
     * Create Polling Service for an SCA Authorization status
     */
    fun createSingleAuthorizationPollingService(): SingleAuthorizationPollingService

    /**
     * Request to confirm SCA Authorization.
     * Result is returned via callback.
     */
    fun confirmAuthorization(
        connectionAndKey: ConnectionAndKey,
        authorizationId: String,
        authorizationCode: String?,
        resultCallback: ConfirmAuthorizationListener
    )

    /**
     * Request to deny SCA Authorization.
     * Result is returned via callback.
     */
    fun denyAuthorization(
        connectionAndKey: ConnectionAndKey,
        authorizationId: String,
        authorizationCode: String?,
        resultCallback: ConfirmAuthorizationListener
    )

    /**
     * Request to send action.
     * Result is returned via callback.
     */
    fun sendAction(
        actionUUID: String,
        connectionAndKey: ConnectionAndKey,
        resultCallback: ActionSubmitListener
    )

    /**
     * Request to get active User Consents list.
     * Result is returned via callback.
     */
    fun getConsents(
        connectionsAndKeys: List<ConnectionAndKey>,
        resultCallback: FetchEncryptedDataListener
    )

    /**
     * Request to revoke consent
     */
    fun revokeConsent(
        consentId: String,
        connectionAndKey: ConnectionAndKey,
        resultCallback: ConsentRevokeListener
    )
}
