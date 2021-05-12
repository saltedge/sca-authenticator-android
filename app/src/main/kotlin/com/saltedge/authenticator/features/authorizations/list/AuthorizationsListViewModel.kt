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

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.*
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.app.ConnectivityReceiverAbs
import com.saltedge.authenticator.app.KEY_OPTION_ID
import com.saltedge.authenticator.app.NetworkStateChangeListener
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.isConnectionNotFound
import com.saltedge.authenticator.core.model.AuthorizationID
import com.saltedge.authenticator.core.model.ConnectionID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.authorizations.common.*
import com.saltedge.authenticator.features.menu.BottomMenuDialog
import com.saltedge.authenticator.features.menu.MenuItemData
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.interfaces.MenuItem
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.api.model.EncryptedData
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationData
import com.saltedge.authenticator.sdk.api.model.authorization.isNotExpired
import com.saltedge.authenticator.sdk.api.model.response.ConfirmDenyResponseData
import com.saltedge.authenticator.sdk.contract.ConfirmAuthorizationListener
import com.saltedge.authenticator.sdk.contract.FetchAuthorizationsContract
import com.saltedge.authenticator.sdk.tools.CryptoToolsAbs
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.getErrorMessage
import com.saltedge.authenticator.tools.postUnitEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthorizationsListViewModel(
    private val appContext: Context,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyManagerAbs,
    private val cryptoTools: CryptoToolsAbs,
    private val apiManager: AuthenticatorApiManagerAbs,
    private val locationManager: DeviceLocationManagerAbs,
    private val connectivityReceiver: ConnectivityReceiverAbs,
    private val defaultDispatcher: CoroutineDispatcher
) : ViewModel(),
    LifecycleObserver,
    ListItemClickListener,
    FetchAuthorizationsContract,
    ConfirmAuthorizationListener,
    TimerUpdateListener,
    NetworkStateChangeListener {

    private var noInternetConnection: Boolean = false
    private var pollingService = apiManager.createAuthorizationsPollingService()
    private var richConnections: Map<ConnectionID, RichConnection> =
        collectRichConnections(connectionsRepository, keyStoreManager)

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
    val onConfirmErrorEvent = MutableLiveData<ViewModelEvent<String>>()
    val onQrScanClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onMoreMenuClickEvent = MutableLiveData<ViewModelEvent<Bundle>>()
    val onShowConnectionsListEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onShowSettingsListEvent = MutableLiveData<ViewModelEvent<Unit>>()

    override fun getCurrentConnectionsAndKeysForPolling(): List<RichConnection>? = collectAuthorizationRequestData()

    override fun onTimeUpdate() {
        listItemsValues.let { items ->
            if (items.any { it.isExpired }) cleanExpiredItems()
            if (items.any { it.shouldBeDestroyed }) cleanDeadItems()
        }
    }

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        val listItem = listItemsValues.getOrNull(itemIndex) ?: return
        val connectionAndKey = richConnections[listItem.connectionID] ?: return
        when (itemViewId) {
            R.id.positiveActionView -> sendConfirmRequest(
                listItem = listItem,
                connectionAndKey = connectionAndKey
            )
            R.id.negativeActionView -> sendDenyRequest(
                listItem = listItem,
                connectionAndKey = connectionAndKey
            )
        }
    }

    override fun onFetchEncryptedDataResult(
        result: List<EncryptedData>,
        errors: List<ApiErrorData>
    ) {
        processAuthorizationsErrors(errors = errors)
        processEncryptedAuthorizationsResult(encryptedList = result)
    }

    override fun onConfirmDenySuccess(result: ConfirmDenyResponseData, connectionID: ConnectionID) {
        findListItem(
            connectionID = connectionID,
            authorizationID = result.authorizationID ?: ""
        )?.let { item ->
            val viewMode = if (item.viewMode == ViewMode.DENY_PROCESSING)
                ViewMode.DENY_SUCCESS else ViewMode.CONFIRM_SUCCESS
            updateItemViewMode(listItem = item, newViewMode = viewMode)
        }
    }

    override fun onConfirmDenyFailure(
        error: ApiErrorData,
        connectionID: ConnectionID,
        authorizationID: AuthorizationID
    ) {
        onConfirmErrorEvent.postValue(ViewModelEvent(error.getErrorMessage(appContext)))
        findListItem(connectionID, authorizationID)?.let { item ->
            updateItemViewMode(listItem = item, newViewMode = ViewMode.ERROR)
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        noInternetConnection = !isConnected
        postMainComponentsState(itemsListIsEmpty = listItemsValues.isEmpty())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        connectivityReceiver.addNetworkStateChangeListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        richConnections = collectRichConnections(connectionsRepository, keyStoreManager)
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
            it.removeObserver(pollingService)
            it.addObserver(pollingService)
        }
        pollingService.contract = this
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

    private fun collectAuthorizationRequestData(): List<RichConnection>? {
        return if (richConnections.isEmpty()) null else richConnections.values.toList()
    }

    private fun processEncryptedAuthorizationsResult(encryptedList: List<EncryptedData>) {
        viewModelScope.launch(defaultDispatcher) {
            val data = decryptAuthorizations(encryptedList = encryptedList)
            withContext(Dispatchers.Main) { processDecryptedAuthorizationsResult(result = data) }
        }
    }

    private fun decryptAuthorizations(encryptedList: List<EncryptedData>): List<AuthorizationData> {
        return encryptedList.mapNotNull {
            cryptoTools.decryptAuthorizationData(
                encryptedData = it,
                rsaPrivateKey = richConnections[it.connectionId]?.private
            )
        }
    }

    private fun processDecryptedAuthorizationsResult(result: List<AuthorizationData>) {
        val newAuthorizationsData = result
            .filter { it.isNotExpired() }
            .sortedWith(compareBy({ it.createdAt }, { it.id }))
        val joinedViewModels = joinViewModels(
            newViewModels = createViewModels(newAuthorizationsData),
            oldViewModels = this.listItemsValues
        )
        if (listItemsValues != joinedViewModels) postListItemsUpdate(newItems = joinedViewModels)
    }

    private fun createViewModels(authorizations: List<AuthorizationData>): List<AuthorizationItemViewModel> {
        return authorizations.mapNotNull { item ->
            richConnections[item.connectionId]?.let {
                item.toAuthorizationItemViewModel(connection = it.connection)
            }
        }
    }

    private fun processAuthorizationsErrors(errors: List<ApiErrorData>) {
        val invalidTokens =
            errors.filter { it.isConnectionNotFound() }.mapNotNull { it.accessToken }
        if (invalidTokens.isNotEmpty()) {
            connectionsRepository.invalidateConnectionsByTokens(accessTokens = invalidTokens)
            richConnections = collectRichConnections(connectionsRepository, keyStoreManager)
        }
    }

    private fun findListItem(
        connectionID: ConnectionID,
        authorizationID: AuthorizationID
    ): AuthorizationItemViewModel? {
        return listItemsValues.find {
            it.authorizationID == authorizationID && it.connectionID == connectionID
        }
    }

    private fun sendConfirmRequest(
        listItem: AuthorizationItemViewModel,
        connectionAndKey: RichConnection
    ) {
        updateItemViewMode(
            listItem = listItem,
            newViewMode = ViewMode.CONFIRM_PROCESSING
        )
        apiManager.confirmAuthorization(
            connectionAndKey = connectionAndKey,
            authorizationId = listItem.authorizationID,
            authorizationCode = listItem.authorizationCode,
            geolocation = locationManager.locationDescription,
            authorizationType = AppTools.lastUnlockType.description,
            resultCallback = this
        )
    }

    private fun sendDenyRequest(
        listItem: AuthorizationItemViewModel,
        connectionAndKey: RichConnection
    ) {
        updateItemViewMode(
            listItem = listItem,
            newViewMode = ViewMode.DENY_PROCESSING
        )
        apiManager.denyAuthorization(
            connectionAndKey = connectionAndKey,
            authorizationId = listItem.authorizationID,
            authorizationCode = listItem.authorizationCode,
            geolocation = locationManager.locationDescription,
            authorizationType = AppTools.lastUnlockType.description,
            resultCallback = this
        )
    }

    private fun updateItemViewMode(listItem: AuthorizationItemViewModel, newViewMode: ViewMode) {
        val itemIndex = listItemsValues.indexOf(listItem)
        listItem.setNewViewMode(newViewMode = newViewMode)
        listItemUpdateEvent.postValue(ViewModelEvent(itemIndex))
    }

    private fun postListItemsUpdate(newItems: List<AuthorizationItemViewModel>) {
        listItems.postValue(newItems)
        postMainComponentsState(itemsListIsEmpty = newItems.isEmpty())
    }

    private fun postMainComponentsState(itemsListIsEmpty: Boolean) {
        val connectionsListIsEmpty = richConnections.isEmpty()
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
            updateItemViewMode(listItem = it, newViewMode = ViewMode.TIME_OUT)
        }
    }

    private fun cleanDeadItems() {
        val currentItems = listItemsValues.filter { !it.shouldBeDestroyed }
        if (currentItems != listItemsValues) postListItemsUpdate(newItems = currentItems)
    }
}
