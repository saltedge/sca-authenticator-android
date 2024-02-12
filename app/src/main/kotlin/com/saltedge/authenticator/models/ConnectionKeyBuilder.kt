/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.models

import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION

/**
 * Collects all active Connections and related Private Keys
 *
 * @param repository data source of connections
 * @param keyStoreManager data source of keys
 * @return Map<ID, ConnectionAndKey>
 */
fun collectRichConnections(
    repository: ConnectionsRepositoryAbs,
    keyStoreManager: KeyManagerAbs,
    apiVersion: String? = null
): Map<ID, RichConnection> {//TODO TEST
    val connections = apiVersion
        ?.let { repository.getAllActiveConnectionsByApi(it)  }
        ?: repository.getAllActiveConnections()
    return connections.mapNotNull { it.toRichConnectionPair(keyStoreManager) }.toMap()
}

/**
 * Find Connections by ID and related Private Key
 *
 * @param connectionID required connection GUID
 * @param repository data source of connections
 * @param keyStoreManager data source of keys
 * @return ConnectionAndKey
 */
fun createRichConnection(
    connectionID: ID,
    repository: ConnectionsRepositoryAbs,
    keyStoreManager: KeyManagerAbs
): RichConnection? = repository.getById(connectionID)?.toRichConnection(keyStoreManager)

/**
 * Create rich connection with related RSA keys
 *
 * @receiver Connection object
 * @param keyStoreManager data source of keys
 * @return RichConnection
 */
fun ConnectionAbs.toRichConnection(keyStoreManager: KeyManagerAbs): RichConnection? {
    return keyStoreManager.enrichConnection(
        connection = this,
        addProviderKey = this.apiVersion == API_V2_VERSION
    )
}

/**
 * Create rich connection Map Entry with related RSA keys
 *
 * @receiver Connection object
 * @param keyStoreManager data source of keys
 * @return Pair<ID, ConnectionAndKey>
 */
fun ConnectionAbs.toRichConnectionPair(
    keyStoreManager: KeyManagerAbs
): Pair<ID, RichConnection>? {
    return this.toRichConnection(keyStoreManager)?.let { model -> Pair(this.id, model) }
}
