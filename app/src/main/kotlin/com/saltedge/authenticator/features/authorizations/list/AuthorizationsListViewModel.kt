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
import android.view.View
import androidx.lifecycle.*
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.authorizations.common.*
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.contract.ConfirmAuthorizationListener
import com.saltedge.authenticator.sdk.contract.FetchAuthorizationsContract
import com.saltedge.authenticator.sdk.model.AuthorizationID
import com.saltedge.authenticator.sdk.model.ConnectionID
import com.saltedge.authenticator.sdk.model.EncryptedData
import com.saltedge.authenticator.sdk.model.authorization.AuthorizationData
import com.saltedge.authenticator.sdk.model.authorization.isNotExpired
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.model.error.getErrorMessage
import com.saltedge.authenticator.sdk.model.error.isConnectionNotFound
import com.saltedge.authenticator.sdk.model.response.ConfirmDenyResponseData
import com.saltedge.authenticator.sdk.tools.crypt.CryptoToolsAbs
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.tools.ResId
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class AuthorizationsListViewModel(
    private val appContext: Context,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyStoreManagerAbs,
    private val cryptoTools: CryptoToolsAbs,
    private val apiManager: AuthenticatorApiManagerAbs
) : ViewModel(),
    LifecycleObserver,
    ListItemClickListener,
    FetchAuthorizationsContract,
    ConfirmAuthorizationListener,
    TimerUpdateListener,
    CoroutineScope
{
    private val decryptJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = decryptJob + Dispatchers.IO
    private var pollingService = apiManager.createAuthorizationsPollingService()
    private var connectionsAndKeys: Map<ConnectionID, ConnectionAndKey> =
        collectConnectionsAndKeys(connectionsRepository, keyStoreManager)

    val listVisibility = MutableLiveData<Int>(View.GONE)
    val emptyViewVisibility = MutableLiveData<Int>(View.VISIBLE)
    val emptyViewActionText = MutableLiveData<ResId>(R.string.actions_scan_qr)
    val emptyViewTitleText = MutableLiveData<ResId>(R.string.authorizations_nothing_confirm)
    val emptyViewDescriptionText = MutableLiveData<ResId>(R.string.authorizations_nothing_confirm_description)
    val listItems = MutableLiveData<List<AuthorizationViewModel>>()
    val listItemsValues: List<AuthorizationViewModel>
        get() = listItems.value ?: emptyList()
    val listItemUpdateEvent = MutableLiveData<ViewModelEvent<Int>>()
    val onConfirmErrorEvent = MutableLiveData<ViewModelEvent<String>>()
    val onQrScanClickEvent = MutableLiveData<ViewModelEvent<Unit>>()

    fun bindLifecycleObserver(lifecycle: Lifecycle) {
        lifecycle.let {
            it.removeObserver(this)
            it.addObserver(this)
            it.removeObserver(pollingService)
            it.addObserver(pollingService)
        }
        pollingService.contract = this
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        connectionsAndKeys = collectConnectionsAndKeys(connectionsRepository, keyStoreManager)
        postMainComponentsState(itemsListIsEmpty = listItemsValues.isEmpty())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        decryptJob.cancel()
    }

    //TODO SET AS PRIVATE AFTER CREATING TEST FOR COROUTINE
    fun processDecryptedAuthorizationsResult(result: List<AuthorizationData>) {
        val newAuthorizationsData = result
            .filter { it.isNotExpired() }
            .sortedWith(compareBy({ it.createdAt }, { it.id }))
        val joinedViewModels = joinViewModels(
            newViewModels = createViewModels(newAuthorizationsData),
            oldViewModels = this.listItemsValues
        )
        if (listItemsValues != joinedViewModels) postListItemsUpdate(newItems = joinedViewModels)
    }

    override fun getCurrentConnectionsAndKeysForPolling(): List<ConnectionAndKey>? = collectAuthorizationRequestData()

    override fun onTimeUpdate() {
        listItemsValues.let { items ->
            if (items.any { it.isExpired }) cleanExpiredItems()
            if (items.any { it.shouldBeDestroyed }) cleanDeadItems()
        }
    }

    fun onEmptyViewActionClick() {
        onQrScanClickEvent.postValue(ViewModelEvent(Unit))
    }

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        val listItem = listItemsValues.getOrNull(itemIndex) ?: return
        val connectionAndKey = connectionsAndKeys[listItem.connectionID] ?: return
        when (itemViewId) {
            R.id.positiveActionView -> sendConfirmRequest(listItem = listItem, connectionAndKey = connectionAndKey)
            R.id.negativeActionView -> sendDenyRequest(listItem = listItem, connectionAndKey = connectionAndKey)
        }
    }

    override fun onFetchEncryptedDataResult(result: List<EncryptedData>, errors: List<ApiErrorData>) {
        processAuthorizationsErrors(errors = errors)
        processEncryptedAuthorizationsResult(encryptedList = result)
    }

    override fun onConfirmDenySuccess(result: ConfirmDenyResponseData, connectionID: ConnectionID) {
        findListItem(connectionID = connectionID, authorizationID = result.authorizationID ?: "")?.let { item ->
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

    private fun collectAuthorizationRequestData(): List<ConnectionAndKey>? {
        return if (connectionsAndKeys.isEmpty()) null else connectionsAndKeys.values.toList()
    }

    private fun processEncryptedAuthorizationsResult(encryptedList: List<EncryptedData>) {
        launch {
            val data = decryptAuthorizations(encryptedList = encryptedList)
            withContext(Dispatchers.Main) { processDecryptedAuthorizationsResult(result = data) }
        }
    }

    private fun decryptAuthorizations(encryptedList: List<EncryptedData>): List<AuthorizationData> {
        return encryptedList.mapNotNull {
            cryptoTools.decryptAuthorizationData(
                encryptedData = it,
                rsaPrivateKey = connectionsAndKeys[it.connectionId]?.key
            )
        }
    }

    private fun createViewModels(authorizations: List<AuthorizationData>): List<AuthorizationViewModel> {
        return authorizations.mapNotNull { item ->
            connectionsAndKeys[item.connectionId]?.let {
                item.toAuthorizationViewModel(connection = it.connection)
            }
        }
    }

    private fun processAuthorizationsErrors(errors: List<ApiErrorData>) {
        val invalidTokens =
            errors.filter { it.isConnectionNotFound() }.mapNotNull { it.accessToken }
        if (invalidTokens.isNotEmpty()) {
            connectionsRepository.invalidateConnectionsByTokens(accessTokens = invalidTokens)
            connectionsAndKeys = collectConnectionsAndKeys(connectionsRepository, keyStoreManager)
        }
    }

    private fun findListItem(
        connectionID: ConnectionID,
        authorizationID: AuthorizationID
    ): AuthorizationViewModel? {
        return listItemsValues.find {
            it.authorizationID == authorizationID && it.connectionID == connectionID
        }
    }

    private fun sendConfirmRequest(listItem: AuthorizationViewModel, connectionAndKey: ConnectionAndKey) {
        updateItemViewMode(
            listItem = listItem,
            newViewMode = ViewMode.CONFIRM_PROCESSING
        )
        apiManager.confirmAuthorization(
            connectionAndKey = connectionAndKey,
            authorizationId = listItem.authorizationID,
            authorizationCode = listItem.authorizationCode,
            resultCallback = this
        )
    }

    private fun sendDenyRequest(listItem: AuthorizationViewModel, connectionAndKey: ConnectionAndKey) {
        updateItemViewMode(
            listItem = listItem,
            newViewMode = ViewMode.DENY_PROCESSING
        )
        apiManager.denyAuthorization(
            connectionAndKey = connectionAndKey,
            authorizationId = listItem.authorizationID,
            authorizationCode = listItem.authorizationCode,
            resultCallback = this
        )
    }

    private fun updateItemViewMode(listItem: AuthorizationViewModel, newViewMode: ViewMode) {
        val itemIndex = listItemsValues.indexOf(listItem)
        listItem.setNewViewMode(newViewMode = newViewMode)
        listItemUpdateEvent.postValue(ViewModelEvent(itemIndex))
    }

    private fun postListItemsUpdate(newItems: List<AuthorizationViewModel>) {
        listItems.postValue(newItems)
        postMainComponentsState(itemsListIsEmpty = newItems.isEmpty())
    }

    private fun postMainComponentsState(itemsListIsEmpty: Boolean) {
        val connectionsListIsEmpty = connectionsAndKeys.isEmpty()
        val emptyViewIsVisible = connectionsListIsEmpty || itemsListIsEmpty

        emptyViewVisibility.postValue(if (emptyViewIsVisible) View.VISIBLE else View.GONE)
        listVisibility.postValue(if (emptyViewIsVisible) View.GONE else View.VISIBLE)
        emptyViewActionText.postValue(
            if (connectionsListIsEmpty) R.string.actions_connect else R.string.actions_scan_qr
        )
        emptyViewTitleText.postValue(
            if (connectionsListIsEmpty) R.string.connections_list_no_connections
            else R.string.authorizations_nothing_confirm
        )
        emptyViewDescriptionText.postValue(
            if (connectionsListIsEmpty) R.string.connections_list_no_connections_description
            else R.string.authorizations_nothing_confirm_description
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
