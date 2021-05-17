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
package com.saltedge.authenticator.features.authorizations.list

import android.os.Bundle
import android.view.View
import androidx.lifecycle.*
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ConnectivityReceiverAbs
import com.saltedge.authenticator.app.KEY_OPTION_ID
import com.saltedge.authenticator.app.NetworkStateChangeListener
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.AuthorizationStatus
import com.saltedge.authenticator.features.authorizations.common.TimerUpdateListener
import com.saltedge.authenticator.features.authorizations.common.joinViewModels
import com.saltedge.authenticator.features.menu.BottomMenuDialog
import com.saltedge.authenticator.features.menu.MenuItemData
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.interfaces.MenuItem
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.postUnitEvent
import kotlinx.coroutines.CoroutineScope

class AuthorizationsListViewModel(
    private val interactorV1: AuthorizationsListInteractorV1,
    private val interactorV2: AuthorizationsListInteractorV2,
    private val connectivityReceiver: ConnectivityReceiverAbs
) : ViewModel(),
    LifecycleObserver,
    ListItemClickListener,
    TimerUpdateListener,
    NetworkStateChangeListener, AuthorizationsListInteractorCallback {

    private var noInternetConnection: Boolean = false

    val listVisibility = MutableLiveData<Int>(View.GONE)
    val emptyViewVisibility = MutableLiveData<Int>(View.GONE)
    val emptyViewImage = MutableLiveData<ResId>(R.drawable.ic_authorizations_empty)
    val emptyViewActionText = MutableLiveData<ResId?>(R.string.actions_scan_qr)
    val emptyViewTitleText = MutableLiveData<ResId>(R.string.authorizations_empty_title)
    val emptyViewDescriptionText = MutableLiveData<ResId>(R.string.authorizations_empty_description)
    val listItems = MutableLiveData<List<AuthorizationItemViewModel>>()
    val listItemsValues: List<AuthorizationItemViewModel>
        get() = listItems.value ?: emptyList()
    val listItemUpdateEvent = MutableLiveData<ViewModelEvent<Int>>()
    val errorEvent = MutableLiveData<ViewModelEvent<ApiErrorData>>()
    val onQrScanClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onMoreMenuClickEvent = MutableLiveData<ViewModelEvent<Bundle>>()
    val onShowConnectionsListEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onShowSettingsListEvent = MutableLiveData<ViewModelEvent<Unit>>()

    init {
        interactorV1.contract = this
        interactorV2.contract = this
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        connectivityReceiver.addNetworkStateChangeListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        interactorV1.updateConnections()
        interactorV2.updateConnections()
        if (listItemsValues.isNotEmpty()) postListItemsUpdate(listItemsValues)
        postMainComponentsState(itemsListIsEmpty = listItemsValues.isEmpty())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        connectivityReceiver.removeNetworkStateChangeListener(this)
    }

    fun bindLifecycleObserver(lifecycle: Lifecycle) {
        lifecycle.let {
            it.removeObserver(this)
            it.addObserver(this)
            interactorV1.bindLifecycleObserver(lifecycle)
            interactorV2.bindLifecycleObserver(lifecycle)
        }
    }

    fun onEmptyViewActionClick() {
        onQrScanClickEvent.postUnitEvent()
    }

    fun onAppbarMenuItemClick(menuItem: MenuItem) {
        when (menuItem) {
            MenuItem.SCAN_QR -> onQrScanClickEvent.postUnitEvent()
            MenuItem.MORE_MENU -> {
                val menuItems = listOf<MenuItemData>(
                    MenuItemData(
                        id = R.string.connections_feature_title,
                        iconRes = R.drawable.ic_menu_action_connections,
                        textRes = R.string.connections_feature_title
                    ),
                    MenuItemData(
                        id = R.string.settings_feature_title,
                        iconRes = R.drawable.ic_menu_action_settings,
                        textRes = R.string.settings_feature_title
                    )
                )
                onMoreMenuClickEvent.postValue(ViewModelEvent(BottomMenuDialog.dataBundle(menuItems = menuItems)))
            }
            else -> Unit
        }
    }

    /**
     * Handle clicks on bottom navigation menu
     */
    fun onItemMenuClicked(data: Bundle?) {
        when (data?.getInt(KEY_OPTION_ID, 0)) {
            R.string.connections_feature_title -> onShowConnectionsListEvent.postUnitEvent()
            R.string.settings_feature_title -> onShowSettingsListEvent.postUnitEvent()
        }
    }

    override fun onTimeUpdate() {
        listItemsValues.let { items ->
            if (items.any { it.isExpired }) cleanExpiredItems()
            if (items.any { it.shouldBeDestroyed }) cleanDeadItems()
        }
    }

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        listItemsValues.getOrNull(itemIndex)?.let {
            when (itemViewId) {
                R.id.positiveActionView -> updateAuthorization(listItem = it, confirm = true)
                R.id.negativeActionView -> updateAuthorization(listItem = it, confirm = false)
            }
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        noInternetConnection = !isConnected
        postMainComponentsState(itemsListIsEmpty = listItemsValues.isEmpty())
    }

    override fun onAuthorizationsReceived(data: List<AuthorizationItemViewModel>) {
        val joinedViewModels = joinViewModels(
            newViewModels = data,
            oldViewModels = this.listItemsValues
        )
        if (listItemsValues != joinedViewModels) postListItemsUpdate(newItems = joinedViewModels)
    }

    override fun onConfirmDenySuccess(connectionID: ID, authorizationID: ID, newStatus: AuthorizationStatus?) {
        findListItem(connectionID = connectionID, authorizationID = authorizationID)?.let { item ->
            val calculatedStatus = if (item.status == AuthorizationStatus.DENY_PROCESSING)
                AuthorizationStatus.DENIED else AuthorizationStatus.CONFIRMED
            updateItemStatus(listItem = item, newStatus = newStatus ?: calculatedStatus)
        }
    }

    override fun onConfirmDenyFailure(error: ApiErrorData, connectionID: ID, authorizationID: ID) {
        errorEvent.postValue(ViewModelEvent(error))
        findListItem(connectionID, authorizationID)?.let { item ->
            updateItemStatus(listItem = item, newStatus = AuthorizationStatus.ERROR)
        }
    }

    override val coroutineScope: CoroutineScope
        get() = viewModelScope

    private fun findListItem(connectionID: ID, authorizationID: ID): AuthorizationItemViewModel? {
        return listItemsValues.find {
            it.authorizationID == authorizationID && it.connectionID == connectionID
        }
    }

    private fun updateAuthorization(listItem: AuthorizationItemViewModel, confirm: Boolean) {
        val result = if (listItem.isV2Api) {
            interactorV1.updateAuthorization(
                connectionID = listItem.connectionID,
                authorizationID = listItem.authorizationID,
                authorizationCode = listItem.authorizationCode,
                confirm = confirm
            )
        } else {
            interactorV2.updateAuthorization(
                connectionID = listItem.connectionID,
                authorizationID = listItem.authorizationID,
                authorizationCode = listItem.authorizationCode,
                confirm = confirm
            )
        }
        if (result) {
            updateItemStatus(
                listItem = listItem,
                newStatus = if (confirm) AuthorizationStatus.CONFIRM_PROCESSING else AuthorizationStatus.DENY_PROCESSING
            )
        }
    }

    private fun updateItemStatus(listItem: AuthorizationItemViewModel, newStatus: AuthorizationStatus) {
        val itemIndex = listItemsValues.indexOf(listItem)
        listItem.setNewStatus(newStatus = newStatus)
        listItemUpdateEvent.postValue(ViewModelEvent(itemIndex))
    }

    private fun postListItemsUpdate(newItems: List<AuthorizationItemViewModel>) {
        listItems.postValue(newItems)
        postMainComponentsState(itemsListIsEmpty = newItems.isEmpty())
    }

    private fun postMainComponentsState(itemsListIsEmpty: Boolean) {
        val connectionsListIsEmpty = interactorV1.noConnections && interactorV2.noConnections
        val emptyViewIsVisible = connectionsListIsEmpty || itemsListIsEmpty

        emptyViewVisibility.postValue(if (emptyViewIsVisible) View.VISIBLE else View.GONE)
        listVisibility.postValue(if (emptyViewIsVisible) View.GONE else View.VISIBLE)
        emptyViewImage.postValue(
            when {
                noInternetConnection -> R.drawable.ic_internet_connection
                connectionsListIsEmpty -> R.drawable.ic_connections_empty
                else -> R.drawable.ic_authorizations_empty
            }
        )
        emptyViewActionText.postValue(
            when {
                noInternetConnection -> null
                connectionsListIsEmpty -> R.string.actions_connect
                else -> R.string.actions_scan_qr
            }
        )
        emptyViewTitleText.postValue(
            when {
                noInternetConnection -> R.string.authorizations_no_internet_title
                connectionsListIsEmpty -> R.string.connections_list_empty_title
                else -> R.string.authorizations_empty_title
            }
        )
        emptyViewDescriptionText.postValue(
            when {
                noInternetConnection -> R.string.authorizations_no_internet_description
                connectionsListIsEmpty -> R.string.connections_list_empty_description
                else -> R.string.authorizations_empty_description
            }
        )
    }

    private fun cleanExpiredItems() {
        listItemsValues.filter { it.shouldBeSetTimeOutMode }.forEach {
            updateItemStatus(listItem = it, newStatus = AuthorizationStatus.TIME_OUT)
        }
    }

    private fun cleanDeadItems() {
        val currentItems = listItemsValues.filter { !it.shouldBeDestroyed }
        if (currentItems != listItemsValues) postListItemsUpdate(newItems = currentItems)
    }
}
