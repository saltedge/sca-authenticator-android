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
package com.saltedge.authenticator.features.authorizations.list

import androidx.lifecycle.Lifecycle
import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.isConnectionNotFound
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.models.collectRichConnections
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import com.saltedge.authenticator.sdk.v2.api.contract.AuthorizationConfirmListener
import com.saltedge.authenticator.sdk.v2.api.contract.AuthorizationDenyListener
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationResponseData
import com.saltedge.authenticator.sdk.v2.api.model.authorization.ConfirmDenyResponseData
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationData
import com.saltedge.authenticator.sdk.v2.polling.PollingAuthorizationsContract
import com.saltedge.authenticator.sdk.v2.tools.CryptoToolsAbs

import kotlinx.coroutines.*

class AuthorizationsListV2Interactor(
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyManagerAbs,
    private val cryptoTools: CryptoToolsAbs,
    private val apiManager: ScaServiceClientAbs,
    private val locationManager: DeviceLocationManagerAbs,
    private val defaultDispatcher: CoroutineDispatcher
) : AuthorizationConfirmListener, AuthorizationDenyListener, PollingAuthorizationsContract {

    var contract: AuthorizationsListInteractorCallback? = null
    val noConnections: Boolean
        get() = richConnections.isEmpty()
    private var pollingService = apiManager.createAuthorizationsPollingService()
    private var richConnections: Map<ID, RichConnection> =
        collectRichConnections(connectionsRepository, keyStoreManager, API_V2_VERSION)

    fun updateConnections() {
        richConnections = collectRichConnections(connectionsRepository, keyStoreManager, API_V2_VERSION)
    }

    fun bindLifecycleObserver(lifecycle: Lifecycle) {
        lifecycle.removeObserver(pollingService)
        lifecycle.addObserver(pollingService)
        pollingService.contract = this
    }

    fun updateAuthorization(connectionID: ID, authorizationID: ID, authorizationCode: String, confirm: Boolean): Boolean {
        val authorizationData = UpdateAuthorizationData(
            authorizationCode = authorizationCode,
            geolocation = locationManager.locationDescription ?: "",
            userAuthorizationType = AppTools.lastUnlockType.description
        )
        if (confirm) {
            apiManager.confirmAuthorization(
                connection = richConnections[connectionID] ?: return false,
                authorizationId = authorizationID,
                authorizationData = authorizationData,
                callback = this
            )
        } else {
            apiManager.denyAuthorization(
                connection = richConnections[connectionID] ?: return false,
                authorizationId = authorizationID,
                authorizationData = authorizationData,
                callback = this
            )
        }
        return true
    }

    override fun getCurrentConnectionsAndKeysForPolling(): List<RichConnection> = richConnections.values.toList()

    override fun onFetchAuthorizationsResult(
        result: List<AuthorizationResponseData>,
        errors: List<ApiErrorData>
    ) {
        processAuthorizationsErrors(errors = errors)
        processEncryptedAuthorizationsResult(encryptedList = result)
    }

    override fun onAuthorizationConfirmSuccess(result: ConfirmDenyResponseData) {
        contract?.onConfirmDenySuccess(connectionID, result.authorizationID)//TODO ERROR
    }

    override fun onAuthorizationConfirmFailure(error: ApiErrorData, authorizationID: ID) {
        contract?.onConfirmDenyFailure(error, connectionID, authorizationID)
    }

    override fun onAuthorizationDenySuccess(result: ConfirmDenyResponseData) {
        contract?.onConfirmDenySuccess(connectionID, result.authorizationID ?: return)//TODO ERROR
    }

    override fun onAuthorizationDenyFailure(error: ApiErrorData, authorizationID: ID) {
        contract?.onConfirmDenyFailure(error, connectionID, authorizationID)
    }

    private fun processAuthorizationsErrors(errors: List<ApiErrorData>) {
        val invalidTokens =
            errors.filter { it.isConnectionNotFound() }.mapNotNull { it.accessToken }
        if (invalidTokens.isNotEmpty()) {
            connectionsRepository.invalidateConnectionsByTokens(accessTokens = invalidTokens)
            richConnections = collectRichConnections(connectionsRepository, keyStoreManager)
        }
    }

    private fun processEncryptedAuthorizationsResult(encryptedList: List<EncryptedData>) {
        contract?.coroutineScope?.launch(defaultDispatcher) {
            val data = decryptAuthorizations(encryptedList = encryptedList)
            withContext(Dispatchers.Main) {
                val newAuthorizationsData = data
                    .filter { it.isNotExpired() }
                    .sortedWith(compareBy({ it.createdAt }, { it.id }))
                contract?.onAuthorizationsReceived(createViewModels(newAuthorizationsData))
            }
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

    private fun createViewModels(authorizations: List<AuthorizationData>): List<AuthorizationItemViewModel> {
        return authorizations.mapNotNull { item ->
            richConnections[item.connectionId]?.let {
                item.toAuthorizationItemViewModel(connection = it.connection)
            }
        }
    }
}
