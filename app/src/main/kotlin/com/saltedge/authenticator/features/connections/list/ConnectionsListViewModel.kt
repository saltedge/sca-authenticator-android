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
import android.util.Log
import android.view.View
import androidx.lifecycle.*
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ConnectivityReceiverAbs
import com.saltedge.authenticator.app.LOCATION_PERMISSION_REQUEST_CODE
import com.saltedge.authenticator.app.guid
import com.saltedge.authenticator.core.api.KEY_NAME
import com.saltedge.authenticator.core.api.model.ConsentData
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.features.connections.common.ConnectionItem
import com.saltedge.authenticator.features.connections.common.convertConnectionsToViewItems
import com.saltedge.authenticator.features.connections.edit.EditConnectionNameDialog
import com.saltedge.authenticator.features.connections.list.menu.ConnectionsListMenuItemType
import com.saltedge.authenticator.features.connections.list.menu.MenuData
import com.saltedge.authenticator.features.connections.list.menu.PopupMenuBuilder
import com.saltedge.authenticator.features.connections.list.menu.buildConnectionsListMenu
import com.saltedge.authenticator.features.consents.list.ConsentsListViewModel
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.postUnitEvent
import kotlinx.coroutines.CoroutineScope
import java.lang.ref.WeakReference

class ConnectionsListViewModel(
    private val weakContext: WeakReference<Context>,
    private val interactor: ConnectionsListInteractorAbs,
    private val locationManager: DeviceLocationManagerAbs,
    private val connectivityReceiver: ConnectivityReceiverAbs
) : ViewModel(),
    ConnectionsListInteractorCallback,
    LifecycleObserver,
    PopupMenuBuilder.ItemClickListener
{
    private val listItemsValues: List<ConnectionItem>
        get() = listItems.value ?: emptyList()
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
    val listItems = MutableLiveData<List<ConnectionItem>>(emptyList())
    val updateListItemEvent = MutableLiveData<ConnectionItem>()
    override val coroutineScope: CoroutineScope
        get() = viewModelScope

    init {
        interactor.contract = this
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        interactor.updateConnections()
        interactor.updateConsents()
    }

    fun refreshConsents() {
        interactor.updateConsents()
    }

    fun onItemNameChanged(data: Bundle) {
        val listItem = listItemsValues.find { it.guid == data.guid } ?: return
        val newConnectionName = data.getString(KEY_NAME) ?: return
        if (listItem.name != newConnectionName
            && newConnectionName.isNotEmpty()
            && interactor.updateNameAndSave(listItem.guid, newConnectionName)
        ) {
            val itemIndex = listItemsValues.indexOf(listItem)
            listItems.value?.get(itemIndex)?.name = newConnectionName
            updateListItemEvent.postValue(listItem)
        }
    }

    fun deleteItem(guid: GUID) {
        val listItem = listItemsValues.find { it.guid == guid } ?: return
        interactor.revokeConnection(connectionGuid = listItem.guid)
    }

    fun onViewClick(viewId: Int) {
        if (viewId == R.id.actionView) onQrScanClickEvent.postUnitEvent()
    }

    fun onListItemClick(itemIndex: Int) {
        val item = listItemsValues.getOrNull(itemIndex) ?: return
        onListItemClickEvent.postValue(
            ViewModelEvent(MenuData(menuId = itemIndex, items = buildConnectionsListMenu(item)))
        )
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
            && grantResults.any { it == PackageManager.PERMISSION_GRANTED }
        ) {
            locationManager.startLocationUpdates()
            interactor.updateConnections()
        }
    }

    override fun onMenuItemClick(menuId: Int, itemId: Int) {
        val item = listItemsValues.getOrNull(menuId) ?: return
        when (ConnectionsListMenuItemType.values()[itemId]) {
            ConnectionsListMenuItemType.RECONNECT -> {
                onReconnectClickEvent.postValue(ViewModelEvent(item.guid))
            }
            ConnectionsListMenuItemType.RENAME -> {
                onRenameClickEvent.postValue(
                    ViewModelEvent(EditConnectionNameDialog.dataBundle(item.guid, item.name))
                )
            }
            ConnectionsListMenuItemType.SUPPORT -> {
                onSupportClickEvent.postValue(ViewModelEvent(item.email))
            }
            ConnectionsListMenuItemType.CONSENTS -> {
                onViewConsentsClickEvent.postValue(
                    ViewModelEvent(ConsentsListViewModel.newBundle(
                        connectionGuid = item.guid,
                        consents = interactor.getConsents(connectionGuid = item.guid)
                    ))
                )
            }
            ConnectionsListMenuItemType.LOCATION -> {
                onAccessToLocationClickEvent.postUnitEvent()
            }
            ConnectionsListMenuItemType.DELETE -> {
                when {
                    !connectivityReceiver.hasNetworkConnection -> {
                        onShowNoInternetConnectionDialogEvent.postValue(ViewModelEvent(item.guid))
                    }
                    item.isActive -> onDeleteClickEvent.postValue(ViewModelEvent(item.guid))
                    else -> interactor.revokeConnection(item.guid)
                }
            }
            else -> {}
        }
    }

    override fun onDatasetChanged(connections: List<ConnectionAbs>, consents: List<ConsentData>) {
        val context = weakContext.get() ?: return
        val items = connections.convertConnectionsToViewItems(context, locationManager)
        val itemsWithConsentInfo = items.enrichItemsWithConsentInfo(consents)
        listItems.postValue(itemsWithConsentInfo)
        emptyViewVisibility.postValue(if (itemsWithConsentInfo.isEmpty()) View.VISIBLE else View.GONE)
        listVisibility.postValue(if (itemsWithConsentInfo.isEmpty()) View.GONE else View.VISIBLE)
    }

    fun onDialogActionClick(dialogActionId: Int, actionResId: ResId, guid: GUID = "") {
        if (dialogActionId == DialogInterface.BUTTON_POSITIVE) {
            when (actionResId) {
                R.string.actions_proceed -> onAskPermissionsEvent.postUnitEvent()
                R.string.actions_go_to_settings -> onGoToSettingsEvent.postUnitEvent()
                R.string.actions_retry -> if (connectivityReceiver.hasNetworkConnection) deleteItem(guid = guid)
            }
        }
    }

    private fun List<ConnectionItem>.enrichItemsWithConsentInfo(
        consents: List<ConsentData>
    ): List<ConnectionItem> {
        return this.onEach { item ->
            item.consentsCount = consents.filter { it.connectionId == item.connectionId  }.count()
        }
    }
}
