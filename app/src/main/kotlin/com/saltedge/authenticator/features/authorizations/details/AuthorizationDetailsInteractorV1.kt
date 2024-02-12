/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.authorizations.details

import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.core.api.model.EncryptedData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.toAuthorizationItemViewModel
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.api.model.response.ConfirmDenyResponseData
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.contract.ConfirmAuthorizationListener
import com.saltedge.authenticator.sdk.polling.FetchAuthorizationContract
import com.saltedge.authenticator.sdk.tools.CryptoToolsV1Abs

class AuthorizationDetailsInteractorV1(
    connectionsRepository: ConnectionsRepositoryAbs,
    keyStoreManager: KeyManagerAbs,
    private val cryptoTools: CryptoToolsV1Abs,
    private val apiManager: AuthenticatorApiManagerAbs
) : AuthorizationDetailsInteractor(
    connectionsRepository = connectionsRepository,
    keyStoreManager = keyStoreManager
), FetchAuthorizationContract, ConfirmAuthorizationListener {
    private var pollingService = apiManager.createSingleAuthorizationPollingService()

    override fun startPolling(authorizationID: ID) {
        pollingService.contract = this
        pollingService.start(authorizationID = authorizationID)
    }

    override fun stopPolling() {
        pollingService.contract = null
        pollingService.stop()
    }

    override fun getConnectionDataForAuthorizationPolling(): RichConnection? = this.richConnection

    override fun onFetchAuthorizationResult(result: EncryptedData?, error: ApiErrorData?) {
        result?.let { processEncryptedAuthorizationResult(it) }
            ?: error?.let { processApiError(it) }
            ?: contract?.onAuthorizationNotFoundError()
    }

    override fun updateAuthorization(
        authorizationID: ID,
        authorizationCode: String,
        confirm: Boolean,
        locationDescription: String?
    ): Boolean {
        if (confirm) {
            apiManager.confirmAuthorization(
                connectionAndKey = richConnection ?: return false,
                authorizationId = authorizationID,
                authorizationCode = authorizationCode,
                geolocation = locationDescription,
                authorizationType = AppTools.lastUnlockType.description,
                resultCallback = this
            )
        } else {
            apiManager.denyAuthorization(
                connectionAndKey = richConnection ?: return false,
                authorizationId = authorizationID,
                authorizationCode = authorizationCode,
                geolocation = locationDescription,
                authorizationType = AppTools.lastUnlockType.description,
                resultCallback = this
            )
        }
        return true
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
}
