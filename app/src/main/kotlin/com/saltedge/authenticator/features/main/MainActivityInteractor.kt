/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.main

import com.saltedge.authenticator.cloud.PushTokenUpdater
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.model.isActive
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.models.toRichConnection
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION

class MainActivityInteractor(
    private val apiManagerV1: AuthenticatorApiManagerAbs,
    private val apiManagerV2: ScaServiceClientAbs,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyManagerAbs,
    private val preferenceRepository: PreferenceRepositoryAbs,
    private val pushTokenUpdater: PushTokenUpdater
) {
    val noConnections: Boolean
        get() = connectionsRepository.isEmpty()

    fun updatePushToken() {
        pushTokenUpdater.updatePushToken()
    }

    fun sendRevokeRequestForConnections() {
        val richConnections: List<RichConnection> = connectionsRepository.getAllActiveConnections()
            .filter { it.isActive() }
            .mapNotNull { it.toRichConnection(keyStoreManager) }
        apiManagerV1.revokeConnections(
            connectionsAndKeys = richConnections.filter { it.connection.apiVersion == API_V1_VERSION },
            resultCallback = null
        )
        apiManagerV2.revokeConnections(
            richConnections = richConnections.filter { it.connection.apiVersion == API_V2_VERSION },
            callback = null
        )
    }

    fun wipeApplication() {
        preferenceRepository.clearUserPreferences()
        keyStoreManager.deleteKeyPairsIfExist(connectionsRepository.getAllConnections().map { it.guid })
        connectionsRepository.deleteAllConnections()
    }
}
