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

import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.isConnectivityError
import com.saltedge.authenticator.core.model.*
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.collectRichConnections
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.toRichConnection
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.api.model.ConsentData
import com.saltedge.authenticator.sdk.api.model.EncryptedData
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.contract.ConnectionsRevokeListener
import com.saltedge.authenticator.sdk.contract.FetchEncryptedDataListener
import com.saltedge.authenticator.sdk.tools.CryptoToolsV1Abs
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ConnectionsListInteractor(
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val apiManagerV1: AuthenticatorApiManagerAbs,
    private val apiManagerV2: ScaServiceClientAbs,
    private val keyStoreManager: KeyManagerAbs,
    private val cryptoTools: CryptoToolsV1Abs
) : FetchEncryptedDataListener, CoroutineScope, ConnectionsRevokeListener,
    com.saltedge.authenticator.sdk.v2.api.contract.ConnectionsRevokeListener {

    var contract: ConnectionsListInteractorCallback? = null
    private var currentConnectionGuid: String = ""
    private val decryptJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = decryptJob + Dispatchers.IO
    private var richConnections: Map<ID, RichConnection> = emptyMap()

    override fun onFetchEncryptedDataResult(
        result: List<EncryptedData>,
        errors: List<ApiErrorData>
    ) {
        processOfEncryptedConsentsResult(encryptedList = result)
    }

    override fun onConnectionsRevokeResult(revokedTokens: List<Token>, apiError: ApiErrorData?) {
        deleteConnectionData(apiError = apiError)
    }

    override fun onConnectionsRevokeResult(apiError: ApiErrorData?) {
        deleteConnectionData(apiError = apiError)
    }

    fun updateConnections() {
        richConnections = collectRichConnections(connectionsRepository, keyStoreManager)
    }

    fun onDestroy() {
        decryptJob.cancel()
    }

    fun getAllConnections(): List<Connection> {
        return connectionsRepository.getAllConnections().sortedBy { it.createdAt }
    }

    fun updateNameAndSave(guid: GUID, newConnectionName: String): Boolean {
        val connection = connectionsRepository.getByGuid(guid) ?: return false
        connectionsRepository.updateNameAndSave(connection, newConnectionName)
        return true
    }

    fun getConsents() {
        val v1RichConnections = richConnections.values.filter { it.connection.apiVersion == API_V1_VERSION }
        if (v1RichConnections.isEmpty()) return
        apiManagerV1.getConsents(connectionsAndKeys = v1RichConnections.toList(), resultCallback = this)
    }

    fun revokeConnection(guid: String) {
        currentConnectionGuid = guid
        val connection = connectionsRepository.getByGuid(guid) ?: return
        sendRevokeRequestForConnection(connection)
    }

    private fun sendRevokeRequestForConnection(connection: Connection) {
        if (!connection.isActive()) {
            deleteConnectionsAndKeys(currentConnectionGuid)
            return
        }
        val richConnection = connection.toRichConnection(keyStoreManager) ?: return
        if (connection.apiVersion == API_V1_VERSION) {
            apiManagerV1.revokeConnections(connectionsAndKeys = listOf(richConnection), resultCallback = this)
        } else if (connection.apiVersion == API_V2_VERSION) {
            apiManagerV2.revokeConnections(connections = listOf(richConnection), callback = this)
        }
    }

    private fun deleteConnectionsAndKeys(guid: String) {
        keyStoreManager.deleteKeyPairIfExist(guid)
        connectionsRepository.deleteConnection(guid)
    }

    private fun processOfEncryptedConsentsResult(encryptedList: List<EncryptedData>) {
        launch {
            val data = decryptConsents(encryptedList = encryptedList)
            withContext(Dispatchers.Main) { processDecryptedConsentsResult(result = data) }
        }
    }

    private fun decryptConsents(encryptedList: List<EncryptedData>): List<ConsentData> {
        return encryptedList.mapNotNull {
            cryptoTools.decryptConsentData(
                encryptedData = it,
                rsaPrivateKey = richConnections[it.connectionId]?.private
            )
        }
    }

    private fun processDecryptedConsentsResult(result: List<ConsentData>) {
        contract?.onConsentsDataChanged(result)
    }

    private fun deleteConnectionData(apiError: ApiErrorData?) {
        if (apiError?.isConnectivityError() == true) return
        deleteConnectionsAndKeys(currentConnectionGuid)
        contract?.onConnectionsDataChanged()
    }
}
