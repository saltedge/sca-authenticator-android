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
import com.saltedge.authenticator.models.collectRichConnections
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.core.api.model.EncryptedData
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationData
import com.saltedge.authenticator.sdk.api.model.authorization.isNotExpired
import com.saltedge.authenticator.sdk.api.model.response.ConfirmDenyResponseData
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.contract.ConfirmAuthorizationListener
import com.saltedge.authenticator.sdk.polling.FetchAuthorizationsContract
import com.saltedge.authenticator.sdk.tools.CryptoToolsV1Abs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthorizationsListInteractorV1(
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyManagerAbs,
    private val cryptoTools: CryptoToolsV1Abs,
    private val apiManager: AuthenticatorApiManagerAbs,
    private val defaultDispatcher: CoroutineDispatcher
) : AuthorizationsListInteractorAbs, FetchAuthorizationsContract, ConfirmAuthorizationListener {

    override var contract: AuthorizationsListInteractorCallback? = null
    override val noConnections: Boolean
        get() = richConnections.isEmpty()
    private var pollingService = apiManager.createAuthorizationsPollingService()
    private var richConnections: Map<ID, RichConnection> = collectRichConnections()

    override fun onResume() {
        richConnections = collectRichConnections()
        pollingService.contract = this
        pollingService.start()
    }

    override fun onStop() {
        pollingService.contract = null
        pollingService.stop()
    }

    override fun updateAuthorization(
        connectionID: ID,
        authorizationID: ID,
        authorizationCode: String,
        confirm: Boolean,
        locationDescription: String?
    ): Boolean {
        val richConnection = richConnections[connectionID] ?: return false
        if (confirm) {
            apiManager.confirmAuthorization(
                connectionAndKey = richConnection,
                authorizationId = authorizationID,
                authorizationCode = authorizationCode,
                geolocation = locationDescription,
                authorizationType = AppTools.lastUnlockType.description,
                resultCallback = this
            )
        } else {
            apiManager.denyAuthorization(
                connectionAndKey = richConnection,
                authorizationId = authorizationID,
                authorizationCode = authorizationCode,
                geolocation = locationDescription,
                authorizationType = AppTools.lastUnlockType.description,
                resultCallback = this
            )
        }
        return true
    }

    override fun getCurrentConnectionsAndKeysForPolling(): List<RichConnection> =
        richConnections.values.toList() //TODO REMOVE

    override fun onFetchEncryptedDataResult(
        result: List<EncryptedData>,
        errors: List<ApiErrorData>
    ) {
        processAuthorizationsErrors(errors = errors)
        processEncryptedAuthorizationsResult(encryptedList = result)
    }

    override fun onConfirmDenySuccess(result: ConfirmDenyResponseData, connectionID: ID) {
        contract?.onConfirmDenySuccess(
            connectionID = connectionID,
            authorizationID = result.authorizationID ?: return
        ) //TODO ERROR
    }

    override fun onConfirmDenyFailure(error: ApiErrorData, connectionID: ID, authorizationID: ID) {
        contract?.onConfirmDenyFailure(error, connectionID, authorizationID)
    }

    private fun processAuthorizationsErrors(errors: List<ApiErrorData>) {
        val invalidTokens = errors.filter { it.isConnectionNotFound() }.mapNotNull { it.accessToken }
        if (invalidTokens.isNotEmpty()) {
            connectionsRepository.invalidateConnectionsByTokens(accessTokens = invalidTokens)
            richConnections = collectRichConnections()
        }
    }

    private fun processEncryptedAuthorizationsResult(encryptedList: List<EncryptedData>) {
        contract?.coroutineScope?.launch(defaultDispatcher) {
            val decryptedList = decryptAuthorizations(encryptedList = encryptedList)
            val newAuthorizationsData = createViewModels(decryptedList.filter { it.isNotExpired() })
            withContext(Dispatchers.Main) {
                contract?.onAuthorizationsReceived(
                    data = newAuthorizationsData,
                    newModelsApiVersion = API_V1_VERSION
                )
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

    private fun collectRichConnections() = collectRichConnections(connectionsRepository, keyStoreManager, API_V1_VERSION)
}
