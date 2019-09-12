/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2019 Salt Edge Inc.
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

import android.content.Context
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.authorizations.common.*
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.contract.FetchAuthorizationContract
import com.saltedge.authenticator.sdk.model.*
import com.saltedge.authenticator.sdk.polling.SingleAuthorizationPollingService
import com.saltedge.authenticator.sdk.tools.CryptoToolsAbs
import com.saltedge.authenticator.sdk.tools.KeyStoreManagerAbs
import com.saltedge.authenticator.tool.secure.fingerprint.BiometricToolsAbs
import org.joda.time.DateTime

class AuthorizationDetailsPresenter(
    appContext: Context,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val cryptoTools: CryptoToolsAbs,
    private val keyStoreManager: KeyStoreManagerAbs,
    biometricTools: BiometricToolsAbs,
    apiManager: AuthenticatorApiManagerAbs
) : BaseAuthorizationPresenter(appContext, biometricTools, apiManager),
    FetchAuthorizationContract {

    var viewContract: AuthorizationDetailsContract.View? = null
    val providerName: String
        get() = currentViewModel?.connectionName ?: ""
    val providerLogo: String
        get() = currentViewModel?.connectionLogoUrl ?: ""
    val startTime: DateTime?
        get() = currentViewModel?.createdAt
    val endTime: DateTime?
        get() = currentViewModel?.expiresAt
    val title: String
        get() {
            return if (confirmRequestIsInProgress) appContext.getString(R.string.authorizations_processing)
            else currentViewModel?.title ?: appContext.getString(R.string.authorizations_fetching)
        }
    val description: String
        get() = currentViewModel?.description ?: ""
    val sessionIsNotExpired: Boolean
        get() = currentViewModel?.isNotExpired() ?: false
    private var pollingService: SingleAuthorizationPollingService =
        apiManager.createSingleAuthorizationPollingService()
    private var authorizationId: String? = null
    private var confirmRequestIsInProgress: Boolean = false
    private val initialValuesValid: Boolean
        get() = currentConnectionAndKey != null && authorizationId?.isNotEmpty() == true

    fun setInitialData(connectionId: String?, authorizationId: String?) {
        super.currentConnectionAndKey = createConnectionAndKey(
            connectionID = connectionId ?: "",
            repository = connectionsRepository,
            keyStoreManager = keyStoreManager
        )
        this.authorizationId = authorizationId
    }

    fun onFragmentResume() {
        startPolling()
        if (sessionIsNotExpired) {
            viewContract?.startTimer()
            updateViewContent()
        }
    }

    fun onFragmentPause() {
        viewContract?.stopTimer()
        stopPolling()
    }

    fun onViewClick(viewId: Int) {
        when (viewId) {
            R.id.positiveActionView, R.id.negativeActionView -> {
                onAuthorizeActionSelected(confirmRequest = viewId == R.id.positiveActionView)
            }
            R.id.mainActionView -> viewContract?.closeView()
        }
    }

    fun onTimerTick() {
        if (currentViewModel?.isNotExpired() == true) {
            viewContract?.updateTimeViews()
        } else {
            viewContract?.closeViewWithTimeOutResults()
        }
    }

    override fun baseViewContract(): BaseAuthorizationViewContract? = viewContract

    override fun getConnectionData(): ConnectionAndKey? = currentConnectionAndKey

    override fun fetchAuthorizationResult(
        result: EncryptedAuthorizationData?,
        error: ApiErrorData?
    ) {
        result?.let { processAuthorizationResult(it) }
        error?.let { processAuthorizationError(it) }
    }

    override fun onConfirmDenySuccess(
        authorizationId: String,
        connectionID: ConnectionID,
        success: Boolean
    ) {
        if (success) {
            viewContract?.closeViewWithSuccessResult(authorizationId)
        }
    }

    override fun updateConfirmProgressState(
        connectionID: ConnectionID,
        authorizationID: AuthorizationID,
        confirmRequestIsInProgress: Boolean
    ) {
        if (confirmRequestIsInProgress) stopPolling() else startPolling()
        this.confirmRequestIsInProgress = confirmRequestIsInProgress
        viewContract?.updateViewContent()
    }

    private fun startPolling() {
        if (initialValuesValid) {
            pollingService.contract = this
            pollingService.start(authorizationId = authorizationId ?: "")
        }
    }

    private fun stopPolling() {
        pollingService.contract = null
        pollingService.stop()
    }

    private fun updateViewContent() {
        viewContract?.setHeaderVisibility(show = currentViewModel != null)
        viewContract?.setContentVisibility(show = currentViewModel != null)
        if (currentViewModel != null) {
            viewContract?.updateViewContent()
        }//TODO show progress or invalid IDs error
    }

    private fun processAuthorizationResult(result: EncryptedAuthorizationData) {
        cryptoTools.decryptAuthorizationData(
            encryptedData = result,
            rsaPrivateKey = currentConnectionAndKey?.key
        )?.let {
            val newViewModel = it.toAuthorizationViewModel(
                connection = currentConnectionAndKey?.connection ?: return
            )
            if (super.currentViewModel != newViewModel) {
                val receivedFirstNotNullModel = super.currentViewModel == null
                super.currentViewModel = newViewModel
                if (receivedFirstNotNullModel && sessionIsNotExpired) {
                    viewContract?.startTimer()
                }
                viewContract?.updateViewContent()
            }
        }
    }

    private fun processAuthorizationError(error: ApiErrorData) {
        if (error.isConnectionNotFound()) {
            currentConnectionAndKey?.connection?.accessToken?.let {
                connectionsRepository.invalidateConnectionsByTokens(accessTokens = listOf(it))
            }
        }
        if (error.isConnectivityError()) {
            viewContract?.showError(error.getErrorMessage(appContext))
        } else {
            viewContract?.closeViewWithErrorResult(error.getErrorMessage(appContext))
        }
    }
}
