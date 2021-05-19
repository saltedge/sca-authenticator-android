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
import com.saltedge.authenticator.sdk.v2.ScaServiceClient
import com.saltedge.authenticator.sdk.v2.api.contract.ConnectionCreateListener
import com.saltedge.authenticator.sdk.v2.api.contract.FetchConfigurationListener
import com.saltedge.authenticator.sdk.v2.api.model.configuration.ConfigurationDataV2
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class ConnectProviderInteractorV2(
    private val appContext: Context,
    keyStoreManager: KeyManagerAbs,
    preferenceRepository: PreferenceRepositoryAbs,
    connectionsRepository: ConnectionsRepositoryAbs,
    private val apiManager: ScaServiceClient,
) : ConnectProviderInteractor(
    keyStoreManager = keyStoreManager,
    preferenceRepository = preferenceRepository,
    connectionsRepository = connectionsRepository,
), FetchConfigurationListener, ConnectionCreateListener {

    override fun requestProviderConfiguration(url: String) {
        apiManager.getProviderConfigurationData(url, callback = this)
    }

    override fun onFetchProviderConfigurationSuccess(result: ConfigurationDataV2) {
        super.setNewConnection(result.toConnection())
    }

    override fun onFetchProviderConfigurationFailure(error: ApiErrorData) {
        super.contract?.onReceiveApiError(error)
    }

    override fun requestCreateConnection(connection: Connection, cloudMessagingToken: String, connectQuery: String?) {
        apiManager.createConnectionRequest(
            appContext = appContext,
            connection = connection,
            pushToken = cloudMessagingToken,
            connectQueryParam = connectQuery,
            callback = this
        )
    }

    override fun onConnectionCreateFailure(error: ApiErrorData) {
        contract?.onReceiveApiError(error)
    }
}

fun ConfigurationDataV2.toConnection(): Connection {
    return Connection().also {
        it.guid = createRandomGuid()
        it.name = this.providerName
        it.code = this.providerId
        it.logoUrl = this.providerLogoUrl
        it.connectUrl = this.scaServiceUrl
        it.status = "${ConnectionStatus.INACTIVE}"
        it.createdAt = DateTime.now().withZone(DateTimeZone.UTC).millis
        it.updatedAt = it.createdAt
        it.supportEmail = this.providerSupportEmail
        it.consentManagementSupported = false
        it.geolocationRequired = this.geolocationRequired
        it.apiVersion = this.apiVersion
        it.providerRsaPublicKeyPem = this.providerPublicKey
    }
}
