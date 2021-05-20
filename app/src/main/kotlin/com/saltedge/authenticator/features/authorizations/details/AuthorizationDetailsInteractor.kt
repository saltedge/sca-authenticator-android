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
package com.saltedge.authenticator.features.authorizations.details

import androidx.lifecycle.Lifecycle
import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.isAuthorizationNotFound
import com.saltedge.authenticator.core.api.model.error.isConnectionNotFound
import com.saltedge.authenticator.core.api.model.error.isConnectivityError
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.toAuthorizationItemViewModel
import com.saltedge.authenticator.models.createRichConnection
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.api.model.EncryptedData
import com.saltedge.authenticator.sdk.api.model.response.ConfirmDenyResponseData
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.contract.ConfirmAuthorizationListener
import com.saltedge.authenticator.sdk.polling.FetchAuthorizationContract
import com.saltedge.authenticator.sdk.polling.SingleAuthorizationPollingService
import com.saltedge.authenticator.sdk.tools.CryptoToolsV1Abs

class AuthorizationDetailsInteractor(
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val cryptoTools: CryptoToolsV1Abs,
    private val keyStoreManager: KeyManagerAbs,
    private val apiManager: AuthenticatorApiManagerAbs,
    private val locationManager: DeviceLocationManagerAbs
) : FetchAuthorizationContract, ConfirmAuthorizationListener {
    var contract: AuthorizationDetailsInteractorCallback? = null
    var richConnection: RichConnection? = null
        private set
    private var pollingService: SingleAuthorizationPollingService = apiManager.createSingleAuthorizationPollingService()

    fun bindLifecycleObserver(lifecycle: Lifecycle) {
        lifecycle.removeObserver(pollingService)
        lifecycle.addObserver(pollingService)
        pollingService.contract = this
    }

    fun setInitialData(connectionID: ID) {
        richConnection = createRichConnection(
            connectionID = connectionID,
            repository = connectionsRepository,
            keyStoreManager = keyStoreManager
        )
    }

    fun startPolling(authorizationID: ID) {
        pollingService.contract = this
        pollingService.start(authorizationID = authorizationID)
    }

    fun stopPolling() {
        pollingService.contract = null
        pollingService.stop()
    }

    fun updateAuthorization(authorizationID: ID, authorizationCode: String, confirm: Boolean): Boolean {
        if (confirm) {
            apiManager.confirmAuthorization(
                connectionAndKey = richConnection ?: return false,
                authorizationId = authorizationID,
                authorizationCode = authorizationCode,
                geolocation = locationManager.locationDescription,
                authorizationType = AppTools.lastUnlockType.description,
                resultCallback = this
            )
        } else {
            apiManager.denyAuthorization(
                connectionAndKey = richConnection ?: return false,
                authorizationId = authorizationID,
                authorizationCode = authorizationCode,
                geolocation = locationManager.locationDescription,
                authorizationType = AppTools.lastUnlockType.description,
                resultCallback = this
            )
        }
        return true
    }

    override fun getConnectionDataForAuthorizationPolling(): RichConnection? = this.richConnection

    override fun onFetchAuthorizationResult(result: EncryptedData?, error: ApiErrorData?) {
        result?.let { processEncryptedAuthorizationResult(it) }
            ?: error?.let { processApiError(it) }
            ?: contract?.onAuthorizationNotFoundError()
    }

    override fun onConfirmDenySuccess(result: ConfirmDenyResponseData, connectionID: ID) {
        contract?.onConfirmDenySuccess()
    }

    override fun onConfirmDenyFailure(error: ApiErrorData, connectionID: ID, authorizationID: ID) {
        processApiError(error)
    }

    private fun processEncryptedAuthorizationResult(result: EncryptedData) {
        val newViewModel: AuthorizationItemViewModel? = cryptoTools.decryptAuthorizationData(
                encryptedData = result,
                rsaPrivateKey = richConnection?.private
            )?.toAuthorizationItemViewModel(connection = richConnection?.connection ?: return)
        contract?.onAuthorizationReceived(newViewModel, API_V1_VERSION)
    }

    private fun processApiError(error: ApiErrorData) {
        when {
            error.isConnectionNotFound() -> {
                richConnection?.connection?.accessToken?.let {
                    connectionsRepository.invalidateConnectionsByTokens(accessTokens = listOf(it))
                }
                stopPolling()
                contract?.onConnectionNotFoundError()
            }
            error.isAuthorizationNotFound() -> {
                stopPolling()
                contract?.onAuthorizationNotFoundError()
            }
            error.isConnectivityError() -> contract?.onConnectivityError(error)
            else -> contract?.onError(error)
        }
    }
}
