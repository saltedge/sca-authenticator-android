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
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.lifecycle.*
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ConnectivityReceiverAbs
import com.saltedge.authenticator.app.LOCATION_PERMISSION_REQUEST_CODE
import com.saltedge.authenticator.app.NetworkStateChangeListener
import com.saltedge.authenticator.app.guid
import com.saltedge.authenticator.core.api.KEY_NAME
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.features.connections.common.ConnectionItem
import com.saltedge.authenticator.features.connections.edit.EditConnectionNameDialog
import com.saltedge.authenticator.features.connections.list.menu.MenuData
import com.saltedge.authenticator.features.connections.list.menu.PopupMenuBuilder
import com.saltedge.authenticator.features.consents.common.consentsCountPrefixForConnection
import com.saltedge.authenticator.features.consents.list.ConsentsListViewModel
import com.saltedge.authenticator.features.menu.MenuItemData
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.sdk.api.model.ConsentData
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.postUnitEvent

class ConnectionsListViewModel(
    private val appContext: Context,
    private val interactor: ConnectionsListInteractor,
    private val locationManager: DeviceLocationManagerAbs,
    private val connectivityReceiver: ConnectivityReceiverAbs
) : ViewModel(),
    ConnectionsListInteractorCallback,
    LifecycleObserver,
    PopupMenuBuilder.ItemClickListener,
    NetworkStateChangeListener {

    private var consents: Map<GUID, List<ConsentData>> = emptyMap()
    private var hasInternetConnection: Boolean = true
    val onQrScanClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onListItemClickEvent = MutableLiveData<ViewModelEvent<MenuData>>()
    val onSupportClickEvent = MutableLiveData<ViewModelEvent<String?>>()
    val onReconnectClickEvent = MutableLiveData<ViewModelEvent<String>>()
    val onRenameClickEvent = MutableLiveData<ViewModelEvent<Bundle>>()
    val onAccessToLocationClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onDeleteClickEvent = MutableLiveData<ViewModelEvent<String>>()
    val onViewConsentsClickEvent = MutableLiveData<ViewModelEvent<Bundle>>()
    val onAskPermissionsEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onGoToSettingsEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onShowNoInternetConnectionDialogEvent = MutableLiveData<ViewModelEvent<GUID>>()
    val listVisibility = MutableLiveData<Int>()
    val emptyViewVisibility = MutableLiveData<Int>()
    val listItems = MutableLiveData<List<ConnectionItem>>()
    val listItemsValues: List<ConnectionItem>
        get() = listItems.value ?: emptyList()
    val updateListItemEvent = MutableLiveData<ConnectionItem>()

    init {
        interactor.contract = this
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        connectivityReceiver.addNetworkStateChangeListener(this)
        interactor.updateConnections()
        updateViewsContent()
        refreshConsents()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        connectivityReceiver.removeNetworkStateChangeListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        interactor.onDestroy()
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        hasInternetConnection = isConnected
    }

    override fun onMenuItemClick(menuId: Int, itemId: Int) {
        val item = listItemsValues.getOrNull(menuId) ?: return
        when (PopupMenuItem.values()[itemId]) {
            PopupMenuItem.RECONNECT -> onReconnectClickEvent.postValue(ViewModelEvent(item.guid))
            PopupMenuItem.RENAME -> {
                onRenameClickEvent.postValue(
                    ViewModelEvent(EditConnectionNameDialog.dataBundle(item.guid, item.name))
                )
            }
            PopupMenuItem.SUPPORT -> onSupportClickEvent.postValue(ViewModelEvent(item.email))
            PopupMenuItem.CONSENTS -> {
                onViewConsentsClickEvent.postValue(
                    ViewModelEvent(ConsentsListViewModel.newBundle(item.guid, consents[item.guid]))
                )
            }
            PopupMenuItem.LOCATION -> onAccessToLocationClickEvent.postUnitEvent()
            PopupMenuItem.DELETE -> {
                if (hasInternetConnection) {
                    if (item.isActive) {
                        onDeleteClickEvent.postValue(ViewModelEvent(item.guid))
                    } else {
                        interactor.revokeConnection(item.guid)
                        updateViewsContent()
                    }
                } else {
                    onShowNoInternetConnectionDialogEvent.postValue(ViewModelEvent(item.guid))
                }
            }
        }
    }

    override fun processDecryptedConsentsResult(result: List<ConsentData>) {
        this.consents = result.groupBy {
            listItemsValues.firstOrNull { viewModel ->
                viewModel.connectionId == it.connectionId
            }?.guid ?: ""
        }
        listItems.postValue(updateItemsWithConsentData(listItemsValues, consents))
    }

    fun onItemNameChanged(data: Bundle) {
        val listItem = listItemsValues.find { it.guid == data.guid } ?: return
        val newConnectionName = data.getString(KEY_NAME) ?: return
        if (listItem.name != newConnectionName
            && newConnectionName.isNotEmpty()
            && interactor.updateNameAndSave(listItem.guid, newConnectionName)) {
            val itemIndex = listItemsValues.indexOf(listItem)
            listItems.value?.get(itemIndex)?.name = newConnectionName
            updateListItemEvent.postValue(listItem)
        }
    }

    fun onItemDeleted(guid: GUID) {
        val listItem = listItemsValues.find { it.guid == guid } ?: return
        interactor.revokeConnection(listItem.guid)
        updateViewsContent()
    }

    fun onViewClick(viewId: Int) {
        if (viewId == R.id.actionView) onQrScanClickEvent.postUnitEvent()
    }

    fun onListItemClick(itemIndex: Int) {
        val item = listItemsValues.getOrNull(itemIndex) ?: return
        val menuItems = mutableListOf<MenuItemData>()
        if (!item.isActive) {
            menuItems.add(MenuItemData(
                id = PopupMenuItem.RECONNECT.ordinal,
                iconRes = R.drawable.ic_menu_reconnect_24dp,
                textRes = R.string.actions_reconnect
            ))
        }
        menuItems.addAll(listOf(
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
        ))
        if (consents[item.guid]?.isNotEmpty() == true) {
            menuItems.add(MenuItemData(
                id = PopupMenuItem.CONSENTS.ordinal,
                iconRes = R.drawable.ic_view_consents_24dp,
                textRes = R.string.actions_view_consents
            ))
        }
        if (item.locationPermissionRequired) {
            menuItems.add(MenuItemData(
                id = PopupMenuItem.LOCATION.ordinal,
                iconRes = R.drawable.ic_view_location_24dp,
                textRes = R.string.actions_view_location
            ))
        }
        menuItems.add(MenuItemData(
            id = PopupMenuItem.DELETE.ordinal,
            iconRes = if (item.isActive) R.drawable.ic_menu_delete_24dp else R.drawable.ic_menu_remove_24dp,
            textRes = if (item.isActive) R.string.actions_delete else R.string.actions_remove
        ))
        onListItemClickEvent.postValue(ViewModelEvent(MenuData(menuId = itemIndex, items = menuItems)))
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
            && grantResults.any { it == PackageManager.PERMISSION_GRANTED }
        ) {
            locationManager.startLocationUpdates(appContext)
            updateViewsContent()
        }
    }

    fun refreshConsents() {
        interactor.getConsents()
    }

    private fun updateViewsContent() {
        val items = interactor.getAllConnections().convertConnectionsToViewModels(appContext, locationManager)
        listItems.postValue(updateItemsWithConsentData(items, consents))
        emptyViewVisibility.postValue(if (items.isEmpty()) View.VISIBLE else View.GONE)
        listVisibility.postValue(if (items.isEmpty()) View.GONE else View.VISIBLE)
    }

    private fun updateItemsWithConsentData(
        items: List<ConnectionItem>,
        consents: Map<ID, List<ConsentData>>
    ): List<ConnectionItem> {
        return items.onEach {
            val count = consents[it.guid]?.count() ?: 0
            it.consentsDescription = consentsCountPrefixForConnection(count, appContext)
        }
    }

    fun onDialogActionClick(dialogActionId: Int, actionResId: ResId, guid: GUID = "") {
        if (dialogActionId == DialogInterface.BUTTON_POSITIVE) {
            when (actionResId) {
                R.string.actions_proceed -> onAskPermissionsEvent.postUnitEvent()
                R.string.actions_go_to_settings -> onGoToSettingsEvent.postUnitEvent()
                R.string.actions_retry -> if (hasInternetConnection) onItemDeleted(guid = guid)
            }
        }
    }

    enum class PopupMenuItem {
        RECONNECT, RENAME, SUPPORT, CONSENTS, DELETE, LOCATION
    }
}
