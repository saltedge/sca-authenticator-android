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

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.*
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.guid
import com.saltedge.authenticator.core.api.KEY_NAME
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.*
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.collectRichConnections
import com.saltedge.authenticator.features.connections.common.ConnectionItemViewModel
import com.saltedge.authenticator.features.connections.edit.EditConnectionNameDialog
import com.saltedge.authenticator.features.connections.list.menu.MenuData
import com.saltedge.authenticator.features.connections.list.menu.PopupMenuBuilder
import com.saltedge.authenticator.features.consents.common.consentsCountPrefixForConnection
import com.saltedge.authenticator.features.consents.list.ConsentsListViewModel
import com.saltedge.authenticator.features.menu.MenuItemData
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.api.model.ConsentData
import com.saltedge.authenticator.sdk.api.model.EncryptedData
import com.saltedge.authenticator.sdk.contract.ConnectionsRevokeListener
import com.saltedge.authenticator.sdk.contract.FetchEncryptedDataListener
import com.saltedge.authenticator.sdk.tools.CryptoToolsAbs
import com.saltedge.authenticator.tools.postUnitEvent
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ConnectionsListViewModel(
    private val appContext: Context,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyManagerAbs,
    private val apiManager: AuthenticatorApiManagerAbs,
    private val cryptoTools: CryptoToolsAbs
) : ViewModel(),
    LifecycleObserver,
    ConnectionsRevokeListener,
    FetchEncryptedDataListener,
    CoroutineScope,
    PopupMenuBuilder.ItemClickListener
{
    private val decryptJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = decryptJob + Dispatchers.IO
    private var connectionsAndKeys: Map<ID, RichConnection> =
        collectRichConnections(connectionsRepository, keyStoreManager)
    private var consents: Map<GUID, List<ConsentData>> = emptyMap()
    val onQrScanClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onListItemClickEvent = MutableLiveData<ViewModelEvent<MenuData>>()
    val onSupportClickEvent = MutableLiveData<ViewModelEvent<String?>>()
    val onReconnectClickEvent = MutableLiveData<ViewModelEvent<String>>()
    val onRenameClickEvent = MutableLiveData<ViewModelEvent<Bundle>>()
    val onDeleteClickEvent = MutableLiveData<ViewModelEvent<String>>()
    val onViewConsentsClickEvent = MutableLiveData<ViewModelEvent<Bundle>>()
    val listVisibility = MutableLiveData<Int>()
    val emptyViewVisibility = MutableLiveData<Int>()
    val listItems = MutableLiveData<List<ConnectionItemViewModel>>()
    val listItemsValues: List<ConnectionItemViewModel>
        get() = listItems.value ?: emptyList()
    val updateListItemEvent = MutableLiveData<ConnectionItemViewModel>()

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        updateViewsContent()
        refreshConsents()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        decryptJob.cancel()
    }

    override fun onConnectionsRevokeResult(revokedTokens: List<Token>, apiError: ApiErrorData?) {}

    override fun onFetchEncryptedDataResult(
        result: List<EncryptedData>,
        errors: List<ApiErrorData>
    ) {
        processOfEncryptedConsentsResult(encryptedList = result)
    }

    override fun onMenuItemClick(menuId: Int, itemId: Int) {
        val item = listItemsValues.getOrNull(menuId) ?: return
        when (PopupMenuItem.values()[itemId]) {
            PopupMenuItem.RECONNECT -> onReconnectClickEvent.postValue(ViewModelEvent(item.guid))
            PopupMenuItem.RENAME -> {
                connectionsRepository.getByGuid(item.guid)?.let { connection ->
                    onRenameClickEvent.postValue(ViewModelEvent(
                        EditConnectionNameDialog.dataBundle(item.guid, connection.name)
                    ))
                }
            }
            PopupMenuItem.SUPPORT -> {
                connectionsRepository.getByGuid(item.guid)?.supportEmail?.let {
                    onSupportClickEvent.postValue(ViewModelEvent(it))
                }
            }
            PopupMenuItem.CONSENTS -> {
                onViewConsentsClickEvent.postValue(ViewModelEvent(
                    ConsentsListViewModel.newBundle(item.guid, consents[item.guid])
                ))
            }
            PopupMenuItem.DELETE -> {
                if (item.isActive) onDeleteClickEvent.postValue(ViewModelEvent(item.guid))
                else {
                    deleteConnectionsAndKeys(item.guid)
                    updateViewsContent()
                }
            }
        }
    }

    fun onEditNameResult(data: Bundle) {
        val listItem = listItemsValues.find { it.guid == data.guid } ?: return
        val newConnectionName = data.getString(KEY_NAME) ?: return
        val itemIndex = listItemsValues.indexOf(listItem)
        if (listItem.name != newConnectionName && newConnectionName.isNotEmpty()) {
            connectionsRepository.getByGuid(listItem.guid)?.let { connection ->
                connectionsRepository.updateNameAndSave(connection, newConnectionName)
                listItems.value?.get(itemIndex)?.name = newConnectionName
                updateListItemEvent.postValue(listItem)
            }
        }
    }

    fun onDeleteItemResult(guid: GUID) {
        val listItem = listItemsValues.find { it.guid == guid } ?: return
        connectionsRepository.getByGuid(listItem.guid)?.let { connection ->
            sendRevokeRequestForConnections(listOf(connection))
        }
        deleteConnectionsAndKeys(listItem.guid)
        updateViewsContent()
    }

    fun onViewClick(viewId: Int) {
        if (viewId == R.id.actionView) onQrScanClickEvent.postUnitEvent()
    }

    fun onListItemClick(itemIndex: Int) {
        listItemsValues.getOrNull(itemIndex)?.let { item ->
            val menuItems = mutableListOf<MenuItemData>()
            if (!item.isActive) {
                menuItems.add(
                    MenuItemData(
                        id = PopupMenuItem.RECONNECT.ordinal,
                        iconRes = R.drawable.ic_menu_reconnect_24dp,
                        textRes = R.string.actions_reconnect
                    )
                )
            }
            menuItems.addAll(
                listOf(
                    MenuItemData(
                        id = PopupMenuItem.RENAME.ordinal,
                        iconRes = R.drawable.ic_menu_edit_24dp,
                        textRes = R.string.actions_rename
                    ),
                    MenuItemData(
                        id = PopupMenuItem.SUPPORT.ordinal,
                        iconRes = R.drawable.ic_contact_support_24dp,
                        textRes = R.string.actions_contact_support
                    )
                )
            )
            if ((consents[item.guid]?.count() ?: 0) > 0) {
                menuItems.add(
                    MenuItemData(
                        id = PopupMenuItem.CONSENTS.ordinal,
                        iconRes = R.drawable.ic_view_consents_24dp,
                        textRes = R.string.actions_view_consents
                    )
                )
            }
            menuItems.add(
                MenuItemData(
                    id = PopupMenuItem.DELETE.ordinal,
                    iconRes = if (item.isActive) R.drawable.ic_menu_delete_24dp else R.drawable.ic_menu_remove_24dp,
                    textRes = if (item.isActive) R.string.actions_delete else R.string.actions_remove
                )
            )
            onListItemClickEvent.postValue(ViewModelEvent(MenuData(menuId = itemIndex, items = menuItems)))
        }
    }

    fun refreshConsents() {
        collectConsentRequestData()?.let {
            apiManager.getConsents(
                connectionsAndKeys = it,
                resultCallback = this
            )
        }
    }

    //TODO SET AS PRIVATE AFTER CREATING TEST FOR COROUTINE
    fun processDecryptedConsentsResult(result: List<ConsentData>) {
        this.consents = result.groupBy {
            listItemsValues.firstOrNull { viewModel ->
                viewModel.connectionId == it.connectionId
            }?.guid ?: ""
        }
        val newListItems = updateItemsWithConsentData(listItemsValues, consents)
        listItems.postValue(newListItems)
    }

    private fun collectConsentRequestData(): List<RichConnection>? {
        return if (connectionsAndKeys.isEmpty()) null else connectionsAndKeys.values.toList()
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
                rsaPrivateKey = connectionsAndKeys[it.connectionId]?.private
            )
        }
    }

    private fun updateViewsContent() {
        val newListItems = collectAllConnectionsViewModels(connectionsRepository, appContext)
        listItems.postValue(updateItemsWithConsentData(newListItems, consents))
        if (newListItems.isEmpty()) {
            emptyViewVisibility.postValue(View.VISIBLE)
            listVisibility.postValue(View.GONE)
        } else {
            listVisibility.postValue(View.VISIBLE)
            emptyViewVisibility.postValue(View.GONE)
        }
    }

    private fun updateItemsWithConsentData(
        items: List<ConnectionItemViewModel>,
        consents: Map<ID, List<ConsentData>>
    ): List<ConnectionItemViewModel> {
        return items.apply {
            forEach {
                val count = consents[it.guid]?.count() ?: 0
                it.consentsDescription = consentsCountPrefixForConnection(count, appContext)
            }
        }
    }

    private fun sendRevokeRequestForConnections(connections: List<Connection>) {
        val connectionsAndKeys: List<RichConnection> = connections.filter { it.isActive() }
            .mapNotNull { keyStoreManager.enrichConnection(it) }

        apiManager.revokeConnections(
            connectionsAndKeys = connectionsAndKeys,
            resultCallback = this
        )
    }

    private fun deleteConnectionsAndKeys(connectionGuid: GUID) {
        keyStoreManager.deleteKeyPairIfExist(connectionGuid)
        connectionsRepository.deleteConnection(connectionGuid)
    }

    enum class PopupMenuItem {
        RECONNECT, RENAME, SUPPORT, CONSENTS, DELETE
    }
}
