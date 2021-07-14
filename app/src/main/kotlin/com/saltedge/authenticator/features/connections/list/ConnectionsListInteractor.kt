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

import com.saltedge.authenticator.core.api.model.ConsentData
import com.saltedge.authenticator.core.api.model.EncryptedData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.isConnectivityError
import com.saltedge.authenticator.core.model.*
import com.saltedge.authenticator.core.tools.secure.BaseCryptoToolsAbs
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.consents.common.decryptConsents
import com.saltedge.authenticator.features.consents.common.requestUpdateConsents
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.toRichConnection
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.contract.ConnectionsRevokeListener
import com.saltedge.authenticator.sdk.contract.FetchEncryptedDataListener
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import com.saltedge.authenticator.sdk.v2.api.contract.ConnectionsV2RevokeListener
import com.saltedge.authenticator.sdk.v2.api.contract.FetchConsentsListener
import kotlinx.coroutines.*

class ConnectionsListInteractor(
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val v1ApiManager: AuthenticatorApiManagerAbs,
    private val v2ApiManager: ScaServiceClientAbs,
    private val keyStoreManager: KeyManagerAbs,
    private val cryptoTools: BaseCryptoToolsAbs,
    private val defaultDispatcher: CoroutineDispatcher
) : ConnectionsListInteractorAbs,
    ConnectionsRevokeListener,
    ConnectionsV2RevokeListener,
    FetchEncryptedDataListener,
    FetchConsentsListener
{
    override var contract: ConnectionsListInteractorCallback? = null
    private var richConnections: List<RichConnection> = emptyList()
    private var consentsV1: List<ConsentData> = emptyList()
    private var consentsV2: List<ConsentData> = emptyList()
    val allConsents: List<ConsentData>
        get() = consentsV1 + consentsV2

    override fun updateConnections() {
        val connections = connectionsRepository.getAllConnections()
        richConnections = connections.mapNotNull { it.toRichConnection(keyStoreManager) }
        notifyDatasetChanges()
    }

    override fun updateNameAndSave(connectionGuid: GUID, newConnectionName: String): Boolean {
        val connection = connectionsRepository.getByGuid(connectionGuid) ?: return false
        connectionsRepository.updateNameAndSave(connection, newConnectionName)
        return true
    }

    override fun updateConsents() {
        requestUpdateConsents(richConnections, v1ApiManager, v2ApiManager, this, this)
    }

    override fun revokeConnection(connectionGuid: GUID) {
        val connection = connectionsRepository.getByGuid(connectionGuid) ?: return
        if (connection.isActive()) {
            sendRevokeRequestForConnection(connection)
        } else {
            deleteConnection(guid = connection.guid)
            updateConnections()
        }
    }

    override fun onConnectionsRevokeResult(revokedTokens: List<Token>, apiErrors: List<ApiErrorData>) {
        val errorTokens = apiErrors.mapNotNull { if (it.isConnectivityError()) null else it.accessToken }
        val allGuidsToRevoke = (revokedTokens + errorTokens).mapConnectionsTokensToGuids()
        deleteConnectionsAndKeysByGuid(allGuidsToRevoke)
        if (allGuidsToRevoke.isNotEmpty()) updateConnections()
    }

    override fun onConnectionsV2RevokeResult(revokedIDs: List<ID>, apiErrors: List<ApiErrorData>) {
        val errorsTokensToRevoke = apiErrors.mapNotNull { if (it.isConnectivityError()) null else it.accessToken }
        val errorGuids = errorsTokensToRevoke.mapConnectionsTokensToGuids()
        val successGuids = revokedIDs.mapConnectionsIDsToGuids()
        deleteConnectionsAndKeysByGuid(successGuids + errorGuids)
        if (errorGuids.isNotEmpty() || successGuids.isNotEmpty()) updateConnections()
    }

    override fun onFetchEncryptedDataResult(result: List<EncryptedData>, errors: List<ApiErrorData>) {
        processOfEncryptedConsentsResult(encryptedList = result, apiVersion = API_V1_VERSION)
    }

    override fun onFetchConsentsV2Result(result: List<EncryptedData>, errors: List<ApiErrorData>) {
        processOfEncryptedConsentsResult(encryptedList = result, apiVersion = API_V2_VERSION)
    }

    override fun getConsents(connectionGuid: GUID): List<ConsentData> {
        return allConsents.filter { it.connectionGuid == connectionGuid }
    }

    private fun sendRevokeRequestForConnection(connection: Connection) {
        val richConnection = connection.toRichConnection(keyStoreManager) ?: return
        if (connection.apiVersion == API_V1_VERSION) {
            v1ApiManager.revokeConnections(connectionsAndKeys = listOf(richConnection), resultCallback = this)
        } else if (connection.apiVersion == API_V2_VERSION) {
            v2ApiManager.revokeConnections(richConnections = listOf(richConnection), callback = this)
        }
    }

    private fun List<Token>.mapConnectionsTokensToGuids(): List<GUID> {
        return richConnections
            .filter { this.contains(it.connection.accessToken) }
            .map { it.connection.guid }
    }

    private fun List<ID>.mapConnectionsIDsToGuids(): List<GUID> {
        return richConnections
            .filter { this.contains(it.connection.id) }
            .map { it.connection.guid }
    }

    private fun deleteConnectionsAndKeysByGuid(revokedGuids: List<GUID>) {
        revokedGuids.forEach { deleteConnection(guid = it) }
    }

    private fun deleteConnection(guid: GUID) {
        keyStoreManager.deleteKeyPairIfExist(guid)
        connectionsRepository.deleteConnection(guid)
    }

    private fun processOfEncryptedConsentsResult(encryptedList: List<EncryptedData>, apiVersion: String) {
        val richConnectionsByVersion = richConnections.filter { it.connection.apiVersion == apiVersion }
        contract?.coroutineScope?.launch(defaultDispatcher) {
            val data = encryptedList.decryptConsents(cryptoTools = cryptoTools, richConnections = richConnectionsByVersion)
            withContext(Dispatchers.Main) {
                processDecryptedConsentsResult(result = data, apiVersion = apiVersion)
            }
        }
    }

    private fun processDecryptedConsentsResult(result: List<ConsentData>, apiVersion: String) {
        when (apiVersion) {
            API_V2_VERSION -> consentsV2 = result
            API_V1_VERSION -> consentsV1 = result
            else -> Unit
        }
        notifyDatasetChanges()
    }

    private fun notifyDatasetChanges() {
        contract?.onDatasetChanged(
            connections = richConnections.map { it.connection }.sortedBy { it.createdAt },
            consents = allConsents
        )
    }
}

interface ConnectionsListInteractorAbs {
    var contract: ConnectionsListInteractorCallback?
    fun updateConnections()
    fun updateNameAndSave(connectionGuid: GUID, newConnectionName: String): Boolean
    fun updateConsents()
    fun revokeConnection(connectionGuid: GUID)
    fun getConsents(connectionGuid: GUID): List<ConsentData>
}

interface ConnectionsListInteractorCallback {
    val coroutineScope: CoroutineScope
    fun onDatasetChanged(connections: List<ConnectionAbs>, consents: List<ConsentData>)
}
