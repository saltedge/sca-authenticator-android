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
    private val modelIsNotExpired: Boolean
        get() = currentViewModel?.isNotExpired() ?: true
    private val modelHasFinalMode: Boolean
        get() = currentViewModel?.hasFinalMode() ?: false
    private var pollingService: SingleAuthorizationPollingService =
        apiManager.createSingleAuthorizationPollingService()
    private var authorizationId: String? = null
    private var authorizationUnavailable: Boolean = false
    private val authorizationAvailable: Boolean
        get() = !authorizationUnavailable
    private val viewMode: AuthorizationContentView.Mode
        get() {
            return if (authorizationUnavailable) AuthorizationContentView.Mode.UNAVAILABLE
            else currentViewModel?.viewMode ?: AuthorizationContentView.Mode.LOADING
        }

    fun setInitialData(connectionId: String?, authorizationId: String?) {
        super.currentConnectionAndKey = createConnectionAndKey(
            connectionID = connectionId ?: "",
            repository = connectionsRepository,
            keyStoreManager = keyStoreManager
        )
        this.authorizationId = authorizationId
        this.authorizationUnavailable = currentConnectionAndKey == null || authorizationId == null || authorizationId.isEmpty()
    }

    fun onFragmentResume() {
        if (modelIsNotExpired && authorizationAvailable) startPolling()
        viewContract?.startTimer()
        updateViewContent()
    }

    fun onFragmentPause() {
        viewContract?.stopTimer()
        stopPolling()
    }

    fun onViewClick(viewId: Int) {
        when (viewId) {
            R.id.positiveActionView -> {
                onAuthorizeActionSelected(
                    requestType = ActionType.CONFIRM,
                    quickConfirmMode = true
                )
            }
            R.id.negativeActionView -> {
                onAuthorizeActionSelected(
                    requestType = ActionType.DENY,
                    quickConfirmMode = true
                )
            }
        }
    }

    fun onTimerTick() {
        currentViewModel?.let { model ->
            when {
                model.shouldBeSetTimeOutMode() -> {
                    model.setNewViewMode(AuthorizationContentView.Mode.TIME_OUT)
                    viewContract?.updateTimeViews()
                    viewContract?.setContentViewMode(viewMode)
                }
                model.shouldBeDestroyed() -> viewContract?.closeView()
                else -> viewContract?.updateTimeViews()
            }
        }
    }

    override fun baseViewContract(): BaseAuthorizationViewContract? = viewContract

    override fun getConnectionData(): ConnectionAndKey? = currentConnectionAndKey

    override fun onFetchAuthorizationResult(
        result: EncryptedAuthorizationData?,
        error: ApiErrorData?
    ) {
        result?.let { processEncryptedAuthorizationResult(it) }
            ?: error?.let { processFetchAuthorizationError(it) }
            ?: setUnavailableState()
    }

    override fun onAuthorizeStart(connectionID: ConnectionID, authorizationID: AuthorizationID, type: ActionType) {
        stopPolling()
        currentViewModel?.setNewViewMode(type.toViewMode())
        viewContract?.setContentViewMode(viewMode)
    }

    override fun onConfirmDenySuccess(
        success: Boolean,
        connectionID: ConnectionID,
        authorizationID: String
    ) {
        val newViewMode = if (success) {
            when (viewMode) {
                AuthorizationContentView.Mode.CONFIRM_PROCESSING -> AuthorizationContentView.Mode.CONFIRM_SUCCESS
                AuthorizationContentView.Mode.DENY_PROCESSING -> AuthorizationContentView.Mode.DENY_SUCCESS
                else -> AuthorizationContentView.Mode.ERROR
            }
        } else {
            startPolling()
            AuthorizationContentView.Mode.DEFAULT
        }
        currentViewModel?.setNewViewMode(newViewMode)
        viewContract?.setContentViewMode(newViewMode)
    }

    override fun onConfirmDenyFailure(
        error: ApiErrorData,
        connectionID: ConnectionID,
        authorizationID: AuthorizationID
    ) {
        processFetchAuthorizationError(error)
    }

    private fun startPolling() {
        if (authorizationAvailable) {
            authorizationId?.let {
                pollingService.contract = this
                pollingService.start(authorizationId = it)
            }
        }
    }

    private fun stopPolling() {
        pollingService.contract = null
        pollingService.stop()
    }

    private fun processEncryptedAuthorizationResult(result: EncryptedAuthorizationData) {
        cryptoTools.decryptAuthorizationData(
            encryptedData = result,
            rsaPrivateKey = currentConnectionAndKey?.key
        )?.let {
            val newViewModel = it.toAuthorizationViewModel(
                connection = currentConnectionAndKey?.connection ?: return
            )
            if (!modelHasFinalMode && super.currentViewModel != newViewModel) {
                if (viewMode == AuthorizationContentView.Mode.LOADING) {
                    viewContract?.startTimer()
                }
                super.currentViewModel = newViewModel
                updateViewContent()
            }
        } ?: setUnavailableState()
    }

    private fun updateViewContent() {
        viewContract?.setHeaderVisibility(show = currentViewModel != null)
        viewContract?.setContentViewMode(viewMode)
        currentViewModel?.let {
            viewContract?.setHeaderValues(
                logo = it.connectionLogoUrl ?: "",
                title = it.connectionName,
                startTime = it.createdAt,
                endTime = it.expiresAt
            )
            viewContract?.setContentTitleAndDescription(title = it.title, description = it.description)
        }
    }

    private fun setUnavailableState() {
        stopPolling()
        currentViewModel?.setNewViewMode(AuthorizationContentView.Mode.UNAVAILABLE)
        authorizationUnavailable = true
        updateViewContent()
    }

    private fun processFetchAuthorizationError(error: ApiErrorData) {
        if (error.isConnectionNotFound()) {
            currentConnectionAndKey?.connection?.accessToken?.let {
                connectionsRepository.invalidateConnectionsByTokens(accessTokens = listOf(it))
            }
            currentViewModel?.setNewViewMode(AuthorizationContentView.Mode.ERROR)
            viewContract?.setContentViewMode(AuthorizationContentView.Mode.ERROR)
        } else if (error.isAuthorizationNotFound()) {
            setUnavailableState()
        } else {
            if (currentViewModel?.viewMode != AuthorizationContentView.Mode.ERROR) {
                viewContract?.showError(error.getErrorMessage(appContext))
                currentViewModel?.setNewViewMode(AuthorizationContentView.Mode.ERROR)
            }
        }
    }
}
