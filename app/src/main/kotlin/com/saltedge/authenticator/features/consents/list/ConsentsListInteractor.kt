/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
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

import com.saltedge.authenticator.core.api.model.ConsentData
import com.saltedge.authenticator.core.api.model.EncryptedData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.BaseCryptoToolsAbs
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.consents.common.decryptConsents
import com.saltedge.authenticator.features.consents.common.requestUpdateConsents
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.toRichConnection
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.contract.FetchEncryptedDataListener
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.contract.FetchConsentsListener
import kotlinx.coroutines.*

class ConsentsListInteractor(
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyManagerAbs,
    private val v1ApiManager: AuthenticatorApiManagerAbs,
    private val v2ApiManager: ScaServiceClientAbs,
    private val cryptoTools: BaseCryptoToolsAbs,
    private val defaultDispatcher: CoroutineDispatcher
) : ConsentsListInteractorAbs, FetchEncryptedDataListener, FetchConsentsListener {

    override var contract: ConsentsListInteractorCallback? = null
    override var consents: List<ConsentData> = emptyList()
    private var optRichConnection: RichConnection? = null

    override fun updateConnection(connectionGuid: GUID?): ConnectionAbs? {
        return connectionsRepository.getByGuid(connectionGuid)?.also {
            optRichConnection = it.toRichConnection(keyStoreManager)
        }
    }

    override fun updateConsents() {
        val richConnection = optRichConnection ?: return
        requestUpdateConsents(listOf(richConnection), v1ApiManager, v2ApiManager, this, this)
    }

    override fun getConsent(consentId: ID): ConsentData? = consents.firstOrNull { it.id == consentId }

    override fun removeConsent(consentId: ID): ConsentData? {
        val removedConsent: ConsentData = getConsent(consentId) ?: return null
        val newConsents: MutableList<ConsentData> = consents.toMutableList()
        newConsents.remove(removedConsent)
        onNewConsentsReceived(newConsents)
        return removedConsent
    }

    override fun onFetchEncryptedDataResult(result: List<EncryptedData>, errors: List<ApiErrorData>) {
        processOfEncryptedConsentsResult(encryptedList = result)
    }

    override fun onFetchConsentsV2Result(result: List<EncryptedData>, errors: List<ApiErrorData>) {
        processOfEncryptedConsentsResult(encryptedList = result)
    }

    private fun processOfEncryptedConsentsResult(encryptedList: List<EncryptedData>) {
        val richConnection = optRichConnection ?: return
        contract?.coroutineScope?.launch(defaultDispatcher) {
            val data = encryptedList.decryptConsents(cryptoTools = cryptoTools, richConnections = listOf(richConnection))
            withContext(Dispatchers.Main) { onNewConsentsReceived(data) }
        }
    }

    private fun onNewConsentsReceived(result: List<ConsentData>) {
        consents = result
        contract?.onDatasetChanged(consents = result)
    }
}

interface ConsentsListInteractorAbs {
    var contract: ConsentsListInteractorCallback?
    var consents: List<ConsentData>
    fun updateConnection(connectionGuid: GUID?): ConnectionAbs?
    fun updateConsents()
    fun getConsent(consentId: ID): ConsentData?
    fun removeConsent(consentId: ID): ConsentData?
}

interface ConsentsListInteractorCallback {
    val coroutineScope: CoroutineScope
    fun onDatasetChanged(consents: List<ConsentData>)
}
