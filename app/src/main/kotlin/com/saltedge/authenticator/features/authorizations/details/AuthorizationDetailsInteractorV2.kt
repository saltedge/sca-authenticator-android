/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.authorizations.details

import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.toAuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.toAuthorizationStatus
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import com.saltedge.authenticator.sdk.v2.api.contract.AuthorizationConfirmListener
import com.saltedge.authenticator.sdk.v2.api.contract.AuthorizationDenyListener
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationResponseData
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationResponseData
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationData
import com.saltedge.authenticator.sdk.v2.polling.PollingAuthorizationContract
import com.saltedge.authenticator.sdk.v2.polling.SingleAuthorizationPollingService
import com.saltedge.authenticator.sdk.v2.tools.CryptoToolsV2Abs

class AuthorizationDetailsInteractorV2(
    connectionsRepository: ConnectionsRepositoryAbs,
    keyStoreManager: KeyManagerAbs,
    private val cryptoTools: CryptoToolsV2Abs,
    private val apiManager: ScaServiceClientAbs
) : AuthorizationDetailsInteractor(
    connectionsRepository = connectionsRepository,
    keyStoreManager = keyStoreManager
), PollingAuthorizationContract, AuthorizationConfirmListener, AuthorizationDenyListener {

    private var pollingService: SingleAuthorizationPollingService =
        apiManager.createSingleAuthorizationPollingService()

    override fun startPolling(authorizationID: ID) {
        pollingService.contract = this
        pollingService.start(authorizationID = authorizationID)
    }

    override fun stopPolling() {
        pollingService.contract = null
        pollingService.stop()
    }

    override fun getConnectionDataForAuthorizationPolling(): RichConnection? = this.richConnection

    override fun onFetchAuthorizationSuccess(result: AuthorizationResponseData) {
        val newStatus = result.status.toAuthorizationStatus()
        if (newStatus?.isFinal() == true) {
            stopPolling()
            contract?.onConfirmDenySuccess(newStatus)
        } else {
            val newViewModel: AuthorizationItemViewModel? = cryptoTools.decryptAuthorizationData(
                encryptedData = result,
                rsaPrivateKey = richConnection?.private
            )?.toAuthorizationItemViewModel(connection = richConnection?.connection ?: return)
            contract?.onAuthorizationReceived(newViewModel, API_V2_VERSION)
        }
    }

    override fun onFetchAuthorizationFailed(error: ApiErrorData?) {
        error?.let { processApiError(it) } ?: contract?.onAuthorizationNotFoundError()
    }

    override fun updateAuthorization(
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
                richConnection = richConnection ?: return false,
                authorizationID = authorizationID,
                authorizationData = authorizationData,
                callback = this
            )
        } else {
            apiManager.denyAuthorization(
                richConnection = richConnection ?: return false,
                authorizationID = authorizationID,
                authorizationData = authorizationData,
                callback = this
            )
        }
        return true
    }

    override fun onAuthorizationConfirmSuccess(
        result: UpdateAuthorizationResponseData,
        connectionID: ID
    ) {
        contract?.onConfirmDenySuccess(result.status.toAuthorizationStatus())
    }

    override fun onAuthorizationConfirmFailure(
        error: ApiErrorData,
        connectionID: ID,
        authorizationID: ID
    ) {
        processApiError(error)
    }

    override fun onAuthorizationDenySuccess(
        result: UpdateAuthorizationResponseData,
        connectionID: ID
    ) {
        contract?.onConfirmDenySuccess(result.status.toAuthorizationStatus())
    }

    override fun onAuthorizationDenyFailure(
        error: ApiErrorData,
        connectionID: ID,
        authorizationID: ID
    ) {
        processApiError(error)
    }
}
