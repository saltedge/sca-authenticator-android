/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.connections.create

import android.content.Context
import com.saltedge.authenticator.core.api.ERROR_CLASS_API_RESPONSE
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
import kotlinx.coroutines.CoroutineDispatcher
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import timber.log.Timber

class ConnectProviderInteractorV2(
    private val appContext: Context,
    keyStoreManager: KeyManagerAbs,
    preferenceRepository: PreferenceRepositoryAbs,
    connectionsRepository: ConnectionsRepositoryAbs,
    private val apiManager: ScaServiceClient,
    defaultDispatcher: CoroutineDispatcher,
) : ConnectProviderInteractor(
    keyStoreManager = keyStoreManager,
    preferenceRepository = preferenceRepository,
    connectionsRepository = connectionsRepository,
    defaultDispatcher = defaultDispatcher,
), FetchConfigurationListener, ConnectionCreateListener {

    override fun requestProviderConfiguration(url: String) {
        apiManager.fetchProviderConfigurationData(url, callback = this)
    }

    override fun onFetchProviderConfigurationSuccess(result: ConfigurationDataV2) {
        try {
            super.setNewConnection(result.toConnection())
        } catch (e: Exception) {
            Timber.e(e, "Error while processing configuration data: $result")
            super.contract?.onReceiveApiError(ApiErrorData(errorClassName = ERROR_CLASS_API_RESPONSE, errorMessage = "Error while processing configuration data"))
        }
    }

    override fun onFetchProviderConfigurationFailure(error: ApiErrorData) {
        super.contract?.onReceiveApiError(error)
    }

    override fun requestCreateConnection(connection: Connection, cloudMessagingToken: String, connectQuery: String?) {
        apiManager.requestCreateConnection(
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
        it.logoUrl = this.providerLogoUrl ?: ""
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
