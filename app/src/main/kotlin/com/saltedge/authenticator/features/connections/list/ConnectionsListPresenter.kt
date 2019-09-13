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
package com.saltedge.authenticator.features.connections.list

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.*
import com.saltedge.authenticator.features.connections.common.ConnectionOptions
import com.saltedge.authenticator.features.connections.common.ConnectionViewModel
import com.saltedge.authenticator.model.db.*
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.KEY_NAME
import com.saltedge.authenticator.sdk.contract.ConnectionsRevokeResult
import com.saltedge.authenticator.sdk.model.ApiErrorData
import com.saltedge.authenticator.sdk.model.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.ConnectionStatus
import com.saltedge.authenticator.sdk.model.GUID
import com.saltedge.authenticator.sdk.tools.KeyStoreManagerAbs
import javax.inject.Inject

class ConnectionsListPresenter @Inject constructor(
    private val appContext: Context,
    private val keyStoreManager: KeyStoreManagerAbs,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val apiManager: AuthenticatorApiManagerAbs
) : ConnectionsListContract.Presenter, ConnectionsRevokeResult {

    override var viewContract: ConnectionsListContract.View? = null

    override fun getListItems(): List<ConnectionViewModel> {
        return collectAllConnectionsViewModels(connectionsRepository, appContext)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null || resultCode != RESULT_OK) return
        when (requestCode) {
            ITEM_OPTIONS_REQUEST_CODE -> {
                ConnectionOptions.values().getOrNull(data.getIntExtra(KEY_OPTION_ID, -1))?.let {
                    data.getStringExtra(KEY_GUID)?.let { guid ->
                        onOptionItemClick(item = it, connectionGuid = guid)
                    }
                }
            }
            RENAME_REQUEST_CODE -> {
                onUserRenamedConnection(
                    connectionGuid = data.getStringExtra(KEY_GUID) ?: return,
                    newConnectionName = data.getStringExtra(KEY_NAME)
                )
            }
            DELETE_REQUEST_CODE -> onUserConfirmedDeleteConnection(
                connectionGuid = data.getStringExtra(
                    KEY_GUID
                ) ?: return
            )
        }
    }

    override fun onListItemClick(connectionGuid: GUID) {
        viewContract?.showOptionsView(
            connectionGuid = connectionGuid,
            options = createOptionsMenuList(connectionGuid),
            requestCode = ITEM_OPTIONS_REQUEST_CODE
        )
    }

    override fun onViewClick(viewId: Int) {
        when (viewId) {
            R.id.connectionsFabView, R.id.mainActionView -> viewContract?.showQrScanView()
        }
    }

    override fun onConnectionsRevokeResult(revokedTokens: List<String>, apiError: ApiErrorData?) {}

    private fun onOptionItemClick(item: ConnectionOptions, connectionGuid: GUID) {
        when (item) {
            ConnectionOptions.REPORT_PROBLEM -> {
                viewContract?.showSupportView(
                    supportEmail = connectionsRepository.getByGuid(connectionGuid)?.supportEmail
                )
            }
            ConnectionOptions.RENAME -> onRenameOptionSelected(connectionGuid = connectionGuid)
            ConnectionOptions.DELETE -> onDeleteOptionsSelected(connectionGuid = connectionGuid)
            ConnectionOptions.RECONNECT -> viewContract?.showConnectView(connectionGuid = connectionGuid)
        }
    }

    private fun onRenameOptionSelected(connectionGuid: GUID) {
        connectionsRepository.getByGuid(connectionGuid)?.let { connection ->
            viewContract?.showConnectionNameEditView(
                connectionGuid = connectionGuid,
                connectionName = connection.name,
                requestCode = RENAME_REQUEST_CODE
            )
        }
    }

    private fun onUserRenamedConnection(connectionGuid: GUID, newConnectionName: String) {
        connectionsRepository.getByGuid(connectionGuid)?.let { connection ->
            if (connection.name != newConnectionName && newConnectionName.isNotEmpty()) {
                connectionsRepository.updateNameAndSave(connection, newConnectionName)
                viewContract?.updateListItemName(connectionGuid, newConnectionName)
            }
        }
    }

    private fun onDeleteOptionsSelected(connectionGuid: GUID) {
        viewContract?.showDeleteConnectionView(
            connectionGuid = connectionGuid,
            requestCode = DELETE_REQUEST_CODE
        )
    }

    private fun onUserConfirmedDeleteConnection(connectionGuid: GUID) {
        connectionsRepository.getByGuid(connectionGuid)?.let { connection ->
            sendRevokeRequestForConnections(listOf(connection))
        }
        deleteConnectionsAndKeys(connectionGuid)
        viewContract?.updateViewContent()
    }

    private fun sendRevokeRequestForConnections(connections: List<Connection>) {
        val connectionsAndKeys: List<ConnectionAndKey> = connections.filter { it.isActive() }
            .mapNotNull { it.toConnectionAndKey(keyStoreManager) }

        apiManager.revokeConnections(connectionsAndKeys = connectionsAndKeys, resultCallback = this)
    }

    private fun deleteConnectionsAndKeys(connectionGuid: GUID) {
        keyStoreManager.deleteKeyPair(connectionGuid)
        connectionsRepository.deleteConnection(connectionGuid)
    }

    private fun createOptionsMenuList(connectionGuid: GUID): Array<ConnectionOptions> {
        return connectionsRepository.getByGuid(connectionGuid)?.let {
            if (it.getStatus() === ConnectionStatus.ACTIVE) {
                arrayOf(
                    ConnectionOptions.RENAME,
                    ConnectionOptions.REPORT_PROBLEM,
                    ConnectionOptions.DELETE
                )
            } else {
                arrayOf(
                    ConnectionOptions.RECONNECT,
                    ConnectionOptions.RENAME,
                    ConnectionOptions.REPORT_PROBLEM,
                    ConnectionOptions.DELETE
                )
            }
        } ?: emptyArray()
    }
}
