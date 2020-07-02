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
package com.saltedge.authenticator.features.consents.list

import android.content.Context
import androidx.lifecycle.*
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.authorizations.common.collectConnectionsAndKeys
import com.saltedge.authenticator.features.connections.common.ConnectionViewModel
import com.saltedge.authenticator.features.connections.list.collectConnectionViewModel
import com.saltedge.authenticator.features.consents.common.ConsentItemViewModel
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.contract.FetchEncryptedDataListener
import com.saltedge.authenticator.sdk.model.ConnectionID
import com.saltedge.authenticator.sdk.model.ConsentData
import com.saltedge.authenticator.sdk.model.EncryptedData
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.tools.crypt.CryptoToolsAbs
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.tools.daysTillExpire
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ConsentsListViewModel(
    private val appContext: Context,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyStoreManagerAbs,
    private val apiManager: AuthenticatorApiManagerAbs,
    private val cryptoTools: CryptoToolsAbs
) : ViewModel(), LifecycleObserver, FetchEncryptedDataListener, CoroutineScope {

    private val decryptJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = decryptJob + Dispatchers.IO

    val listItems = MutableLiveData<List<ConsentItemViewModel>>()
    val connectionItem = MutableLiveData<ConnectionViewModel>()

    private var connection = Connection()
    private var connectionsAndKeys: Map<ConnectionID, ConnectionAndKey> =
        collectConnectionsAndKeys(
            connectionsRepository,
            keyStoreManager
        )
    var onListItemClickEvent = MutableLiveData<ViewModelEvent<Int>>()
        private set

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        decryptJob.cancel()
    }

    override fun onFetchEncryptedDataResult(
        result: List<EncryptedData>,
        errors: List<ApiErrorData>
    ) {
        processOfEncryptedConsentsResult(encryptedList = result)
    }

    fun setInitialData(connectionGuid: String?, consents: List<ConsentData>?) {
        listItems.postValue(
            listOf(
                ConsentItemViewModel(
                    id = "id",
                    tppName = "Example Dashboard",
                    consentTypeDescription = "aisp",
                    expiresAt = "7 days"
                ) //TODO: fix why data is not converted correctly with consents.buildViewModels()
            )
        )
        if (connectionGuid != null) {
            this.connection = connectionsRepository.getByGuid(connectionGuid) ?: Connection()
            val connectionViewModel = collectConnectionViewModel(connectionGuid, connectionsRepository, appContext)
            connectionItem.postValue(connectionViewModel)
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

    private fun collectConsentRequestData(): List<ConnectionAndKey>? {
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
                rsaPrivateKey = connectionsAndKeys[it.connectionId]?.key
            )
        }
    }

    //TODO SET AS PRIVATE AFTER CREATING TEST FOR COROUTINE
    fun processDecryptedConsentsResult(result: List<ConsentData>) {
        val consents = listOf(
            ConsentItemViewModel(
                id = "id",
                tppName = "Example Dashboard",
                consentTypeDescription = "aisp",
                expiresAt = "7 days"
            ) //TODO: fix why data is not converted correctly with consents.buildViewModels()
        )
        listItems.postValue(consents)
        if (consents.isNotEmpty()) {
            appContext.resources.getQuantityString(
                R.plurals.ui_consents,
                consents.size,
                consents.size
            ) + " ·"
        } else {
            ""
        }
    }

    fun onListItemClick(itemIndex: Int) {
        onListItemClickEvent.postValue(ViewModelEvent(itemIndex))
    }

    private fun buildViewModels(data: List<ConsentData>): List<ConsentItemViewModel> {
        return data.map {
            ConsentItemViewModel(
                id = it.id,
                tppName = it.tppName,
                consentTypeDescription = it.consentType,
                expiresAt = it.expiresAt.daysTillExpire().toString()
            )
        }
    }
}
