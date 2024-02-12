/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.settings.list

import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.model.isActive
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.toRichConnection
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs

class SettingsListInteractorV2(
    private val keyStoreManager: KeyManagerAbs,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val apiManager: ScaServiceClientAbs
) {

    fun sendRevokeRequestForConnections() {
        val richConnections: List<RichConnection> = connectionsRepository.getAllActiveConnections()
            .filter { it.isActive() }
            .mapNotNull { it.toRichConnection(keyStoreManager) }
        apiManager.revokeConnections(richConnections = richConnections, callback = null)
    }

    fun deleteAllConnectionsAndKeys() {
        keyStoreManager.deleteKeyPairsIfExist(connectionsRepository.getAllConnections().map { it.guid })
        connectionsRepository.deleteAllConnections()
    }
}
