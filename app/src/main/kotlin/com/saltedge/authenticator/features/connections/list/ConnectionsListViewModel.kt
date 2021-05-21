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
import com.saltedge.authenticator.core.model.*
import com.saltedge.authenticator.sdk.api.model.ConsentData
import com.saltedge.authenticator.features.connections.common.ConnectionItemViewModel
import com.saltedge.authenticator.features.connections.edit.EditConnectionNameDialog
import com.saltedge.authenticator.features.connections.list.menu.MenuData
import com.saltedge.authenticator.features.connections.list.menu.PopupMenuBuilder
import com.saltedge.authenticator.features.consents.common.consentsCountPrefixForConnection
import com.saltedge.authenticator.features.consents.list.ConsentsListViewModel
import com.saltedge.authenticator.features.menu.MenuItemData
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.tools.postUnitEvent

class ConnectionsListViewModel(
    private val appContext: Context,
    private val interactorV1: ConnectionsListInteractorV1,
    private val interactorV2: ConnectionsListInteractorV2,
) : ViewModel(),
    ConnectionsListInteractorCallback,
    LifecycleObserver,
    PopupMenuBuilder.ItemClickListener {

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

    init {
        interactorV1.contract = this
        interactorV2.contract = this
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        updateViewsContent()
        refreshConsents()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        interactorV1.onDestroy()
    }

    override fun onMenuItemClick(menuId: Int, itemId: Int) {
        val item = listItemsValues.getOrNull(menuId) ?: return
        when (PopupMenuItem.values()[itemId]) {
            PopupMenuItem.RECONNECT -> onReconnectClickEvent.postValue(ViewModelEvent(item.guid))
            PopupMenuItem.RENAME -> {
                if (item.isV2Api)
                    interactorV2.renameConnection(item.guid)
                else
                    interactorV1.renameConnection(item.guid)
            }
            PopupMenuItem.SUPPORT -> {
                if (item.isV2Api)
                    interactorV2.getConnectionSupportEmail(item.guid)
                else
                    interactorV1.getConnectionSupportEmail(item.guid)
            }
            PopupMenuItem.CONSENTS -> {
                onViewConsentsClickEvent.postValue(
                    ViewModelEvent(
                        ConsentsListViewModel.newBundle(item.guid, consents[item.guid])
                    )
                )
            }
            PopupMenuItem.DELETE -> {
                if (item.isActive) onDeleteClickEvent.postValue(ViewModelEvent(item.guid))
                else {
                    if (item.isV2Api)
                        interactorV2.deleteConnectionsAndKeys(item.guid)
                    else
                        interactorV1.deleteConnectionsAndKeys(item.guid)
                    updateViewsContent()
                }
            }
        }
    }

    override fun renameConnection(guid: String, name: String) {
        onRenameClickEvent.postValue(
            ViewModelEvent(
                EditConnectionNameDialog.dataBundle(guid, name)
            )
        )
    }

    override fun selectSupportForConnection(guid: String) {
        onSupportClickEvent.postValue(ViewModelEvent(guid))
    }

    override fun processDecryptedConsentsResult(result: List<ConsentData>) {
        this.consents = result.groupBy {
            listItemsValues.firstOrNull { viewModel ->
                viewModel.connectionId == it.connectionId
            }?.guid ?: ""
        }
        val newListItems = updateItemsWithConsentData(listItemsValues, consents)
        listItems.postValue(newListItems)
    }

    fun onEditNameResult(data: Bundle) {
        val listItem = listItemsValues.find { it.guid == data.guid } ?: return
        val newConnectionName = data.getString(KEY_NAME) ?: return
        if (listItem.name != newConnectionName && newConnectionName.isNotEmpty()) {
            if (listItem.isV2Api) {
                interactorV2.updateNameAndSave(listItem, newConnectionName)
            } else {
                interactorV1.updateNameAndSave(listItem, newConnectionName)
            }
        }
    }

    override fun updateName(newConnectionName: String, listItem: ConnectionItemViewModel) {
        val itemIndex = listItemsValues.indexOf(listItem)
        listItems.value?.get(itemIndex)?.name = newConnectionName
        updateListItemEvent.postValue(listItem)
    }

    fun onDeleteItemResult(guid: GUID) {
        val listItem = listItemsValues.find { it.guid == guid } ?: return
        if (listItem.isV2Api)
            interactorV2.sendRevokeRequestForConnections(listItem.guid)
        else
            interactorV1.sendRevokeRequestForConnections(listItem.guid)
        interactorV2.sendRevokeRequestForConnections(listItem.guid)
        interactorV1.deleteConnectionsAndKeys(listItem.guid)
        interactorV2.deleteConnectionsAndKeys(listItem.guid)
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
            onListItemClickEvent.postValue(
                ViewModelEvent(
                    MenuData(
                        menuId = itemIndex,
                        items = menuItems
                    )
                )
            )
        }
    }

    fun refreshConsents() {
        interactorV1.getConsents()
    }

    private fun updateViewsContent() {
        val newListItems = interactorV1.collectAllConnectionsViewModels() //TODO: collectAllConnectionsViewModels in each interactor collect the models it needs, without using a filter in them(maybe create 1 interactor)
        listItems.postValue(
            updateItemsWithConsentData(
                newListItems.convertConnectionsToViewModels(
                    appContext
                ), consents
            )
        )
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

    enum class PopupMenuItem {
        RECONNECT, RENAME, SUPPORT, CONSENTS, DELETE
    }
}
