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
package com.saltedge.authenticator.features.connections.list

import com.saltedge.authenticator.core.model.ConnectionID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.model.isActive
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.authorizations.common.collectRichConnections
import com.saltedge.authenticator.features.connections.common.ConnectionItemViewModel
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.api.model.ConsentData
import com.saltedge.authenticator.sdk.tools.CryptoToolsAbs

abstract class ConnectionsListInteractor(
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyManagerAbs,
    val cryptoTools: CryptoToolsAbs
) : ConnectionsListInteractorAbs {

    override var contract: ConnectionsListInteractorCallback? = null
    var connectionsAndKeys: Map<ConnectionID, RichConnection> =
        collectRichConnections(connectionsRepository, keyStoreManager)

    override fun getConnection(guid: String) {
        connectionsRepository.getByGuid(guid)?.let { connection ->
            contract?.renameConnection(guid = guid, name = connection.name)
        }
    }

    override fun getConnectionSupportEmail(guid: String) {
        connectionsRepository.getByGuid(guid)?.supportEmail?.let {
            contract?.selectSupportForConnection(guid)
        }
    }

    override fun updateNameAndSave(listItem: ConnectionItemViewModel, newConnectionName: String) {
        connectionsRepository.getByGuid(listItem.guid)?.let { connection ->
            connectionsRepository.updateNameAndSave(connection, newConnectionName)
            contract?.updateName(newConnectionName, listItem)
        }
    }

    override fun sendRevokeRequestForConnections(guid: String) {
        connectionsRepository.getByGuid(guid)?.let { connection ->
            sendRevokeRequestForConnections(listOf(connection))
        }
    }

    private fun sendRevokeRequestForConnections(connections: List<Connection>) {
        val connectionsAndKeys: List<RichConnection> = connections.filter { it.isActive() }
            .mapNotNull { keyStoreManager.enrichConnection(it) }
        revokeConnections(connectionsAndKeys = connectionsAndKeys)
    }

    abstract override fun revokeConnections(connectionsAndKeys: List<RichConnection>)

    override fun getConsents() {
        val consentRequestData = if (connectionsAndKeys.isEmpty()) null else connectionsAndKeys.values.toList()
        consentRequestData?.let {
            getConsents(connectionsAndKeys = it)
        }
    }

    override fun deleteConnectionsAndKeys(guid: String) {
        keyStoreManager.deleteKeyPairIfExist(guid)
        connectionsRepository.deleteConnection(guid)
    }

    override fun collectAllConnectionsViewModels(): List<Connection> {
        return connectionsRepository.getAllConnections()
            .sortedBy { it.createdAt }
    }
}

interface ConnectionsListInteractorAbs {
    fun getConnection(guid: String)
    fun getConnectionSupportEmail(guid: String)
    fun updateNameAndSave(listItem: ConnectionItemViewModel, newConnectionName: String)
    fun sendRevokeRequestForConnections(guid: String)
    fun revokeConnections(connectionsAndKeys: List<RichConnection>)
    fun getConsents(connectionsAndKeys: List<RichConnection>) {}
    fun getConsents()
    fun deleteConnectionsAndKeys(guid: String)
    fun collectAllConnectionsViewModels(): List<Connection>
    fun onDestroy() {}

    var contract: ConnectionsListInteractorCallback?
}

interface ConnectionsListInteractorCallback {
    fun renameConnection(guid: String, name: String)
    fun selectSupportForConnection(guid: String)
    fun updateName(newConnectionName: String, listItem: ConnectionItemViewModel)
    fun processDecryptedConsentsResult(result: List<ConsentData>)
}
