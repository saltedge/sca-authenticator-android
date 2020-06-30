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
package com.saltedge.authenticator.features.consents

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.saltedge.authenticator.features.authorizations.common.collectConnectionsAndKeys
import com.saltedge.authenticator.features.connections.common.ConnectionViewModel
import com.saltedge.authenticator.features.connections.list.ConnectionsListViewModel
import com.saltedge.authenticator.features.connections.list.collectAllConnectionsViewModels
import com.saltedge.authenticator.features.connections.list.collectConnectionViewModel
import com.saltedge.authenticator.features.consents.common.ConsentViewModel
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.model.ConnectionID
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.tools.crypt.CryptoToolsAbs
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs

class ConsentsListViewModel(
    private val appContext: Context,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyStoreManagerAbs,
    private val apiManager: AuthenticatorApiManagerAbs,
    private val cryptoTools: CryptoToolsAbs
) : ViewModel(), LifecycleObserver {

    private var connection = Connection()
    private var connectionGuid = ""

    val listItems = MutableLiveData<List<ConsentViewModel>>()
    val listItemsValues: List<ConsentViewModel>
        get() = listItems.value ?: emptyList()
    val connectionItem = MutableLiveData<ConnectionViewModel>()

    private var connectionsAndKeys: Map<ConnectionID, ConnectionAndKey> =
        collectConnectionsAndKeys(
            connectionsRepository,
            keyStoreManager
        )

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        updateViewsContent()
        refreshConsents()
    }

    private fun updateViewsContent() {
        val newListItems = listOf<ConsentViewModel>(ConsentViewModel("id", "Fentury", "Access to account information"))
        listItems.postValue(newListItems)
        val connection = collectConnectionViewModel(connectionGuid, connectionsRepository, appContext)
        connectionItem.postValue(connection)
    }


    fun setInitialData(connectionGuid: String?) {
        if (connectionGuid != null) {
            this.connection = connectionsRepository.getByGuid(connectionGuid) ?: Connection()
            this.connectionGuid = connectionGuid
        }
    }

    fun refreshConsents() {
//        collectConsentRequestData()?.let {
//            apiManager.getConsents(
//                connectionsAndKeys = it,
//                resultCallback = this
//            )
//        }
    }

    private fun collectConsentRequestData(): List<ConnectionAndKey>? {
        return if (connectionsAndKeys.isEmpty()) null else connectionsAndKeys.values.toList()
    }

}
