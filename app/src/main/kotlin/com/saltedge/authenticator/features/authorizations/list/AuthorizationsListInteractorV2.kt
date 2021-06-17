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

import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.isConnectionNotFound
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.toAuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.toAuthorizationStatus
import com.saltedge.authenticator.features.connections.list.shouldRequestPermission
import com.saltedge.authenticator.models.collectRichConnections
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import com.saltedge.authenticator.sdk.v2.api.contract.AuthorizationConfirmListener
import com.saltedge.authenticator.sdk.v2.api.contract.AuthorizationDenyListener
import com.saltedge.authenticator.sdk.v2.api.model.authorization.*
import com.saltedge.authenticator.sdk.v2.polling.PollingAuthorizationsContract
import com.saltedge.authenticator.sdk.v2.tools.CryptoToolsV2Abs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthorizationsListInteractorV2(
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyManagerAbs,
    private val cryptoTools: CryptoToolsV2Abs,
    private val apiManager: ScaServiceClientAbs,
    private val defaultDispatcher: CoroutineDispatcher
) : AuthorizationConfirmListener, AuthorizationDenyListener, PollingAuthorizationsContract {

    var contract: AuthorizationsListInteractorCallback? = null
    val noConnections: Boolean
        get() = richConnections.isEmpty()
    private var pollingService = apiManager.createAuthorizationsPollingService()
    private var richConnections: Map<ID, RichConnection> = collectRichConnections()

    fun shouldRequestPermission(connectionId: ID, locationPermissionsIsGranted: Boolean): Boolean {
        val richConnection: RichConnection? = richConnections[connectionId]
        return shouldRequestPermission(
            geolocationRequired = richConnection?.connection?.geolocationRequired,
            locationPermissionsAreGranted = locationPermissionsIsGranted
        )
    }

    fun onResume() {
        richConnections = collectRichConnections()
        pollingService.contract = this
        pollingService.start()
    }

    fun onStop() {
        pollingService.contract = null
        pollingService.stop()
    }

    fun updateAuthorization(
        connectionID: ID,
        authorizationID: ID,
        authorizationCode: String,
        confirm: Boolean,
        locationDescription: String?
    ): Boolean {
        val authorizationData = UpdateAuthorizationData(
            authorizationCode = authorizationCode,
            geolocation = locationDescription ?: "",
            userAuthorizationType = AppTools.lastUnlockType.description
        )
        if (confirm) {
            apiManager.confirmAuthorization(
                richConnection = richConnections[connectionID] ?: return false,
                authorizationID = authorizationID,
                authorizationData = authorizationData,
                callback = this
            )
        } else {
            apiManager.denyAuthorization(
                richConnection = richConnections[connectionID] ?: return false,
                authorizationID = authorizationID,
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

    override fun onAuthorizationConfirmSuccess(result: UpdateAuthorizationResponseData, connectionID: ID) {
        contract?.onConfirmDenySuccess(
            connectionID = connectionID,
            authorizationID = result.authorizationID,
            newStatus = result.status.toAuthorizationStatus()
        )
    }

    override fun onAuthorizationConfirmFailure(error: ApiErrorData, connectionID: ID, authorizationID: ID) {
        contract?.onConfirmDenyFailure(
            error = error,
            connectionID = connectionID,
            authorizationID = authorizationID
        )
    }

    override fun onAuthorizationDenySuccess(result: UpdateAuthorizationResponseData, connectionID: ID) {
        contract?.onConfirmDenySuccess(
            connectionID = connectionID,
            authorizationID = result.authorizationID,
            newStatus = result.status.toAuthorizationStatus()
        )
    }

    override fun onAuthorizationDenyFailure(error: ApiErrorData, connectionID: ID, authorizationID: ID) {
        contract?.onConfirmDenyFailure(
            error = error,
            connectionID = connectionID,
            authorizationID = authorizationID
        )
    }

    private fun processAuthorizationsErrors(errors: List<ApiErrorData>) {
        val invalidTokens = errors.filter { it.isConnectionNotFound() }.mapNotNull { it.accessToken }
        if (invalidTokens.isNotEmpty()) {
            connectionsRepository.invalidateConnectionsByTokens(accessTokens = invalidTokens)
            richConnections = collectRichConnections()
        }
    }

    private fun processEncryptedAuthorizationsResult(encryptedList: List<AuthorizationResponseData>) {
        contract?.coroutineScope?.launch(defaultDispatcher) {
            val data: List<AuthorizationV2Data> = decryptAuthorizations(encryptedList = encryptedList)
            withContext(Dispatchers.Main) {
                val newAuthorizationsData = data
                    .filter { it.isNotExpired() }
                    .sortedWith(compareBy { it.createdAt })
                contract?.onAuthorizationsReceived(
                    data = createViewModels(newAuthorizationsData),
                    newModelsApiVersion = API_V2_VERSION
                )
            }
        }
    }

    private fun decryptAuthorizations(encryptedList: List<AuthorizationResponseData>): List<AuthorizationV2Data> {
        return encryptedList.mapNotNull {
            cryptoTools.decryptAuthorizationData(
                encryptedData = it,
                rsaPrivateKey = richConnections[it.connectionId]?.private
            )
        }
    }

    private fun createViewModels(authorizations: List<AuthorizationV2Data>): List<AuthorizationItemViewModel> {
        return authorizations.mapNotNull { item ->
            richConnections[item.connectionID]?.let {
                item.toAuthorizationItemViewModel(connection = it.connection)
            }
        }
    }

    private fun collectRichConnections() = collectRichConnections(connectionsRepository, keyStoreManager, API_V2_VERSION)
}
