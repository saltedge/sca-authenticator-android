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
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.model.Token
import com.saltedge.authenticator.core.model.isActive
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.connections.common.ConnectionItemViewModel
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.collectRichConnections
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.api.model.ConsentData
import com.saltedge.authenticator.sdk.api.model.EncryptedData
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.contract.ConnectionsRevokeListener
import com.saltedge.authenticator.sdk.contract.FetchEncryptedDataListener
import com.saltedge.authenticator.sdk.tools.CryptoToolsV1Abs
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ConnectionsListInteractorV1(
    private val apiManager: AuthenticatorApiManagerAbs,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyManagerAbs,
    private val cryptoTools: CryptoToolsV1Abs
) : ConnectionsRevokeListener, FetchEncryptedDataListener, CoroutineScope {

    var contract: ConnectionsListInteractorCallback? = null
    private val decryptJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = decryptJob + Dispatchers.IO
    private var richConnections: Map<ID, RichConnection> =
        collectRichConnections(connectionsRepository, keyStoreManager, API_V1_VERSION)

    override fun onConnectionsRevokeResult(revokedTokens: List<Token>, apiError: ApiErrorData?) {}

    override fun onFetchEncryptedDataResult(
        result: List<EncryptedData>,
        errors: List<ApiErrorData>
    ) {
        processOfEncryptedConsentsResult(encryptedList = result)
    }

    fun onDestroy() {
        decryptJob.cancel()
    }

    fun renameConnection(guid: String) {
        connectionsRepository.getByGuid(guid)?.let { connection ->
            contract?.renameConnection(guid = guid, name = connection.name)
        }
    }

    fun getConnectionSupportEmail(guid: String) {
        connectionsRepository.getByGuid(guid)?.supportEmail?.let {
            contract?.selectSupportForConnection(guid)
        }
    }

    fun updateNameAndSave(listItem: ConnectionItemViewModel, newConnectionName: String) {
        connectionsRepository.getByGuid(listItem.guid)?.let { connection ->
            connectionsRepository.updateNameAndSave(connection, newConnectionName)
            contract?.updateName(newConnectionName, listItem)
        }
    }

    fun sendRevokeRequestForConnections(guid: String) {
        connectionsRepository.getByGuid(guid)?.let { connection ->
            sendRevokeRequestForConnections(listOf(connection))
        }
    }

    fun getConsents() {
        val consentRequestData = if (richConnections.isEmpty()) null else richConnections.values.toList()
        consentRequestData?.let {
            apiManager.getConsents(
                connectionsAndKeys = it,
                resultCallback = this
            )
        }
    }

    fun collectAllConnectionsViewModels(): List<Connection> {
        return connectionsRepository.getAllConnections()
            .sortedBy { it.createdAt }
    }

    fun deleteConnectionsAndKeys(guid: String) {
        keyStoreManager.deleteKeyPairIfExist(guid)
        connectionsRepository.deleteConnection(guid)
    }

    private fun sendRevokeRequestForConnections(connections: List<Connection>) {
        val connectionsAndKeys: List<RichConnection> = connections.filter { it.isActive() }
            .mapNotNull { keyStoreManager.enrichConnection(it) }
        apiManager.revokeConnections(
            connectionsAndKeys = connectionsAndKeys,
            resultCallback = this
        )
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
        contract?.processDecryptedConsentsResult(result)
    }
}
