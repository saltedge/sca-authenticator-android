/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.*
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.*
import com.saltedge.authenticator.features.connections.common.ConnectionViewModel
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.KEY_NAME
import com.saltedge.authenticator.sdk.contract.ConnectionsRevokeListener
import com.saltedge.authenticator.sdk.model.GUID
import com.saltedge.authenticator.sdk.model.Token
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.connection.ConnectionStatus
import com.saltedge.authenticator.sdk.model.connection.getStatus
import com.saltedge.authenticator.sdk.model.connection.isActive
import com.saltedge.authenticator.sdk.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import javax.inject.Inject

class ConnectionsListViewModel @Inject constructor(
    private val appContext: Context,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyStoreManagerAbs,
    private val apiManager: AuthenticatorApiManagerAbs
) : ViewModel(), LifecycleObserver, ConnectionsRevokeListener, PopupMenu.OnMenuItemClickListener {

    var onQrScanClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
        private set
    var onOptionsClickEvent = MutableLiveData<ViewModelEvent<Int>>()
        private set
    var onSupportClickEvent = MutableLiveData<ViewModelEvent<String?>>()
        private set
    var onReconnectClickEvent = MutableLiveData<ViewModelEvent<String>>()
        private set
    var onRenameClickEvent = MutableLiveData<ViewModelEvent<Bundle>>()
        private set
    var onDeleteClickEvent = MutableLiveData<ViewModelEvent<Bundle>>()
        private set

    val listVisibility = MutableLiveData<Int>()
    val emptyViewVisibility = MutableLiveData<Int>()

    val listItems = MutableLiveData<List<ConnectionViewModel>>()
    val listItemsValues: List<ConnectionViewModel>
        get() = listItems.value ?: emptyList()
    val listItemUpdateEvent = MutableLiveData<ViewModelEvent<Int>>()

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        updateViewsContent()
    }

    override fun onConnectionsRevokeResult(revokedTokens: List<Token>, apiError: ApiErrorData?) {}

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val index = onOptionsClickEvent.value?.getContent() ?: return false
        val connectionGuid = listItemsValues.getOrNull(index)?.guid ?: return false
        return when (item?.itemId) {
            R.id.rename -> {
                onRenameOptionSelected(connectionGuid)
                true
            }
            R.id.contact_support -> {
                onSupportClickEvent.postValue(
                    ViewModelEvent(
                        connectionsRepository.getByGuid(
                            connectionGuid
                        )?.supportEmail
                    )
                )
                true
            }
            R.id.delete -> {
                onDeleteOptionsSelected(connectionGuid)
                true
            }
            R.id.reconnect -> {
                onReconnectClickEvent.postValue(ViewModelEvent(connectionGuid))
                true
            }
            else -> false
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null || resultCode != Activity.RESULT_OK) return
        val listItem = listItemsValues.find { it.guid == data.getStringExtra(KEY_GUID) } ?: return
        when (requestCode) {
            RENAME_REQUEST_CODE -> {
                onUserRenamedConnection(
                    listItem = listItem,
                    newConnectionName = data.getStringExtra(KEY_NAME) ?: return
                )
            }
            DELETE_REQUEST_CODE -> onUserConfirmedDeleteConnection(listItem = listItem)
        }
    }

    fun onViewClick(viewId: Int) {
        if (viewId == R.id.actionView) onQrScanClickEvent.postValue(ViewModelEvent(Unit))
    }

    fun onListItemClick(itemIndex: Int) {
        onOptionsClickEvent.postValue(ViewModelEvent(itemIndex))
    }

    fun isReconnect(): Boolean {
        val index = onOptionsClickEvent.value?.getContent() ?: return false
        val connectionGuid = listItemsValues.getOrNull(index)?.guid ?: return false
        return connectionsRepository.getByGuid(connectionGuid)?.let {
            it.getStatus() === ConnectionStatus.ACTIVE
        } ?: false
    }

    private fun onRenameOptionSelected(connectionGuid: GUID) {
        connectionsRepository.getByGuid(connectionGuid)?.let { connection ->
            onRenameClickEvent.postValue(ViewModelEvent(Bundle()
                .apply { putString(KEY_GUID, connectionGuid) }
                .apply { putString(KEY_NAME, connection.name) })
            )
        }
    }

    private fun onDeleteOptionsSelected(connectionGuid: GUID) {
        onDeleteClickEvent.postValue(ViewModelEvent(Bundle()
            .apply { putString(KEY_GUID, connectionGuid) }
        ))
    }

    private fun onUserRenamedConnection(listItem: ConnectionViewModel, newConnectionName: String) {
        val itemIndex = listItemsValues.indexOf(listItem)
        if (listItem.name != newConnectionName && newConnectionName.isNotEmpty()) {
            connectionsRepository.getByGuid(listItem.guid)?.let { connection ->
                connectionsRepository.updateNameAndSave(connection, newConnectionName)
                listItems.value?.get(itemIndex)?.name = newConnectionName
                listItemUpdateEvent.postValue(ViewModelEvent(itemIndex))
            }
        }
    }

    private fun onUserConfirmedDeleteConnection(listItem: ConnectionViewModel) {
        connectionsRepository.getByGuid(listItem.guid)?.let { connection ->
            sendRevokeRequestForConnections(listOf(connection))
        }
        deleteConnectionsAndKeys(listItem.guid)
        updateViewsContent()
    }

    private fun updateViewsContent() {
        val newListItems = collectAllConnectionsViewModels(connectionsRepository, appContext)
        listItems.postValue(newListItems)
        if (newListItems.isEmpty()) {
            emptyViewVisibility.postValue(View.VISIBLE)
            listVisibility.postValue(View.GONE)
        } else {
            listVisibility.postValue(View.VISIBLE)
            emptyViewVisibility.postValue(View.GONE)
        }
    }

    private fun sendRevokeRequestForConnections(connections: List<Connection>) {
        val connectionsAndKeys: List<ConnectionAndKey> = connections.filter { it.isActive() }
            .mapNotNull { keyStoreManager.createConnectionAndKeyModel(it) }

        apiManager.revokeConnections(
            connectionsAndKeys = connectionsAndKeys,
            resultCallback = this
        )
    }

    private fun deleteConnectionsAndKeys(connectionGuid: GUID) {
        keyStoreManager.deleteKeyPair(connectionGuid)
        connectionsRepository.deleteConnection(connectionGuid)
    }
}
