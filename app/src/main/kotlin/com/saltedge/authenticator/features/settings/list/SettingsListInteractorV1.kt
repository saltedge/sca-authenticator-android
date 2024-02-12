/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.settings.list

import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.model.isActive
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.toRichConnection
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs

class SettingsListInteractorV1(
    private val keyStoreManager: KeyManagerAbs,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val apiManager: AuthenticatorApiManagerAbs
) {

    fun sendRevokeRequestForConnections() {
        val connectionsAndKeys: List<RichConnection> =
            connectionsRepository.getAllActiveConnections()
                .filter { it.isActive() }
                .mapNotNull { it.toRichConnection(keyStoreManager) }
        apiManager.revokeConnections(connectionsAndKeys = connectionsAndKeys, resultCallback = null)
    }

    fun deleteAllConnectionsAndKeys() {
        keyStoreManager.deleteKeyPairsIfExist(connectionsRepository.getAllConnections().map { it.guid })
        connectionsRepository.deleteAllConnections()
    }
}
