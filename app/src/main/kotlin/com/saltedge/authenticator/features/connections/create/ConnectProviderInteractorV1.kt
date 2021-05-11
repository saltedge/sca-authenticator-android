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

import android.content.Context
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.tools.createRandomGuid
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.api.model.configuration.ProviderConfigurationData
import com.saltedge.authenticator.sdk.api.model.configuration.isValid
import com.saltedge.authenticator.sdk.api.model.response.CreateConnectionResponseData
import com.saltedge.authenticator.sdk.contract.ConnectionCreateListener
import com.saltedge.authenticator.sdk.contract.FetchProviderConfigurationListener
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class ConnectProviderInteractorV1(
    private val appContext: Context,
    keyStoreManager: KeyManagerAbs,
    preferenceRepository: PreferenceRepositoryAbs,
    connectionsRepository: ConnectionsRepositoryAbs,
    private val apiManager: AuthenticatorApiManagerAbs,
) : ConnectProviderInteractor(
    keyStoreManager = keyStoreManager,
    preferenceRepository = preferenceRepository,
    connectionsRepository = connectionsRepository
), FetchProviderConfigurationListener, ConnectionCreateListener {

    override fun requestProviderConfiguration(url: String) {
        apiManager.getProviderConfigurationData(url, resultCallback = this)
    }

    override fun onFetchProviderConfigurationSuccess(result: ProviderConfigurationData) {
        super.setNewConnection(result.toConnection())
    }

    override fun onFetchProviderConfigurationFailure(error: ApiErrorData) {
        postError(error)
    }

    override fun requestCreateConnection(connection: Connection, cloudMessagingToken: String, connectQuery: String?) {
        apiManager.createConnectionRequest(
            appContext = appContext,
            connection = connection,
            pushToken = cloudMessagingToken,
            connectQueryParam = connectQuery,
            resultCallback = this
        )
    }

    override fun onConnectionCreateFailure(error: ApiErrorData) {
        postError(error)
    }

    override fun onConnectionCreateSuccess(response: CreateConnectionResponseData) {
        val accessToken = response.accessToken

        if (accessToken?.isNotEmpty() == true) {
            onConnectionSuccessAuthentication(
                connectionId = response.connectionId ?: "",
                accessToken = accessToken
            )
        } else {
            onConnectionCreateSuccess(
                authenticationUrl = response.redirectUrl ?: "",
                connectionId = response.connectionId ?: ""
            )
        }
    }

    private fun postError(error: ApiErrorData) {
        super.contract?.onReceiveApiError(ApiErrorData(
            errorClassName = error.errorClassName,
            errorMessage = error.errorMessage,
            accessToken = error.accessToken
        ))
    }
}

fun ProviderConfigurationData.toConnection(): Connection? {
    if (!this.isValid()) return null
    return Connection().also {
        it.guid = createRandomGuid()
        it.name = this.name
        it.code = this.code
        it.logoUrl = this.logoUrl ?: ""
        it.connectUrl = this.connectUrl
        it.status = "${ConnectionStatus.INACTIVE}"
        it.createdAt = DateTime.now().withZone(DateTimeZone.UTC).millis
        it.updatedAt = it.createdAt
        it.supportEmail = this.supportEmail
        it.consentManagementSupported = this.consentManagementSupported
        it.geolocationRequired = this.geolocationRequired
    }
}
