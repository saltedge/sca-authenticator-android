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
import android.util.Log
import android.view.View
import androidx.lifecycle.*
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.*
import com.saltedge.authenticator.features.authorizations.common.AuthorizationViewModel
import com.saltedge.authenticator.features.connections.common.ConnectionOptions
import com.saltedge.authenticator.features.connections.common.ConnectionViewModel
import com.saltedge.authenticator.features.menu.MenuItemData
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
) : ViewModel(), LifecycleObserver, ConnectionsRevokeListener {

    var onQrScanClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
        private set
    var onOptionsClickEvent = MutableLiveData<ViewModelEvent<Bundle>>()
        private set
    var onSupportClickEvent = MutableLiveData<ViewModelEvent<String?>>()
        private set
    var onReconnectClickEvent = MutableLiveData<ViewModelEvent<String>>()
        private set
    var onRenameClickEvent = MutableLiveData<ViewModelEvent<Bundle>>()
        private set
    var onDeleteClickEvent = MutableLiveData<ViewModelEvent<Bundle>>()
        private set

    val onOptionsClickEvent2 = MutableLiveData<ViewModelEvent<List<MenuItemData>>>()


    val listVisibility = MutableLiveData<Int>()
    val emptyViewVisibility = MutableLiveData<Int>()


    val listItems = MutableLiveData<List<ConnectionViewModel>>()
    val listItemsValues: List<ConnectionViewModel>
        get() = listItems.value ?: emptyList()


    //    val listItemUpdateEvent = MutableLiveData<ViewModelEvent<Int>>()


    @OnLifecycleEvent(Lifecycle.Event.ON_START) //TODO: mb in onResume
    fun onStart() {
        updateViewsContent()
    }

    //TODO: Check if we need it
    override fun onConnectionsRevokeResult(revokedTokens: List<Token>, apiError: ApiErrorData?) {}

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null || resultCode != Activity.RESULT_OK) return
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
                    newConnectionName = data.getStringExtra(KEY_NAME) ?: return
                )
            }
            DELETE_REQUEST_CODE -> onUserConfirmedDeleteConnection(
                connectionGuid = data.getStringExtra(KEY_GUID) ?: return
            )
        }
    }

    fun onViewClick(viewId: Int) {
        if (viewId == R.id.actionView) onQrScanClickEvent.postValue(ViewModelEvent(Unit))
    }

    fun onListItemClick(connectionGuid: GUID) { //TODO: resolve options
        //        onOptionsClickEvent.postValue(ViewModelEvent(Bundle()
        //            .apply { putString(KEY_GUID, connectionGuid) }
        //            .apply { putString("options", createOptionsMenuList(connectionGuid)) })
        //        ))

        val menuItems = listOf<MenuItemData>(
            MenuItemData(
                id = R.string.connections_feature_title,
                iconResId = R.drawable.ic_menu_action_list,
                textResId = R.string.connections_feature_title
            ),
            MenuItemData(
                id = R.string.consents_feature_title,
                iconResId = R.drawable.ic_menu_action_list,
                textResId = R.string.consents_feature_title
            ),
            MenuItemData(
                id = R.string.settings_feature_title,
                iconResId = R.drawable.ic_menu_action_settings,
                textResId = R.string.settings_feature_title
            )
        )
//        onOptionsClickEvent.postValue(ViewModelEvent(Bundle()
//            .apply { putParcelableArrayList("options", menuItems) })
//        )
        onOptionsClickEvent2.postValue(ViewModelEvent(menuItems))
    }

    fun getListItems(): List<ConnectionViewModel> {
        return collectAllConnectionsViewModels(connectionsRepository, appContext)
    }

    private fun onOptionItemClick(item: ConnectionOptions, connectionGuid: GUID) {
        when (item) {
            ConnectionOptions.REPORT_PROBLEM -> {
                onSupportClickEvent.postValue(
                    ViewModelEvent(
                        connectionsRepository.getByGuid(
                            connectionGuid
                        )?.supportEmail
                    )
                )
            }
            ConnectionOptions.RENAME -> onRenameOptionSelected(connectionGuid = connectionGuid) // onRenameOptionEvent
            ConnectionOptions.DELETE -> onDeleteOptionsSelected(connectionGuid = connectionGuid) //onDeleteOptionEvent
            ConnectionOptions.RECONNECT ->
                onReconnectClickEvent.postValue(ViewModelEvent(connectionGuid)) //onReconnectClickEvent
        }
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

    private fun onUserRenamedConnection(connectionGuid: GUID, newConnectionName: String) {
        connectionsRepository.getByGuid(connectionGuid)?.let { connection ->
            if (connection.name != newConnectionName && newConnectionName.isNotEmpty()) {
                connectionsRepository.updateNameAndSave(connection, newConnectionName)

                //                viewContract?.updateListItemName(connectionGuid, newConnectionName) //TODO: do smth

                //                val itemIndex = listItemsValues.indexOf(listItem)
                //                listItem.setNewViewMode(newViewMode = newConnectionName)
                //                listItemUpdateEvent.postValue(ViewModelEvent(itemIndex))
            }
        }
    }

    private fun onUserConfirmedDeleteConnection(connectionGuid: GUID) {
        connectionsRepository.getByGuid(connectionGuid)?.let { connection ->
            sendRevokeRequestForConnections(listOf(connection))
        }
        deleteConnectionsAndKeys(connectionGuid)
        updateViewsContent()
    }

    private fun updateViewsContent() {
        getListItems()
        listItems.postValue(getListItems())
        if (listItemsValues.isNotEmpty()) { //TODO: replace on isEmpty
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
