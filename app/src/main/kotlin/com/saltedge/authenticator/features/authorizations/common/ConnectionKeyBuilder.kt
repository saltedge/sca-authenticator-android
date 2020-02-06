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
package com.saltedge.authenticator.features.authorizations.common

import com.saltedge.authenticator.model.db.Connection
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.model.ConnectionAbs
import com.saltedge.authenticator.sdk.model.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.ConnectionID
import com.saltedge.authenticator.sdk.model.getRelatedPrivateKey
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs

/**
 * Collects all active Connections and related Private Keys
 *
 * @param repository data source of connections
 * @param keyStoreManager data source of keys
 * @return Map<ConnectionID, ConnectionAndKey)
 */
fun collectConnectionsAndKeys(
    repository: ConnectionsRepositoryAbs,
    keyStoreManager: KeyStoreManagerAbs
): Map<ConnectionID, ConnectionAndKey> {
    return repository.getAllActiveConnections().mapNotNull {
        it.getPrivateKeyForConnection(keyStoreManager)
    }.toMap()
}

/**
 * Find Connections by ID and related Private Key
 *
 * @param connectionID required connection GUID
 * @param repository data source of connections
 * @param keyStoreManager data source of keys
 * @return ConnectionAndKey
 */
fun createConnectionAndKey(
    connectionID: ConnectionID,
    repository: ConnectionsRepositoryAbs,
    keyStoreManager: KeyStoreManagerAbs
): ConnectionAndKey? {
    return repository.getById(connectionID)?.let { connection ->
        connection.getRelatedPrivateKey(keyStoreManager)?.let { key ->
            ConnectionAndKey(connection, key)
        }
    }
}

/**
 *  Find Connections by related Private Key
 *
 *  @param connection Connection
 *  @param keyStoreManager data source of keys
 *  @return ConnectionAndKey
 */
fun createConnectionAndKey(
    connection: ConnectionAbs,
    keyStoreManager: KeyStoreManagerAbs
): ConnectionAndKey? {
    return connection.getRelatedPrivateKey(keyStoreManager)?.let { key ->
        ConnectionAndKey(connection, key)
    }
}

/**
 * Find Private Key related to Connection
 *
 * @receiver Connection object
 * @param keyStoreManager data source of keys
 * @return Pair<ConnectionID, ConnectionAndKey)
 */
private fun Connection.getPrivateKeyForConnection(
    keyStoreManager: KeyStoreManagerAbs
): Pair<ConnectionID, ConnectionAndKey>? {
    return keyStoreManager.getKeyPair(this.guid)?.let { pair ->
        Pair(this.id, ConnectionAndKey(connection = this, key = pair.private))
    }
}
