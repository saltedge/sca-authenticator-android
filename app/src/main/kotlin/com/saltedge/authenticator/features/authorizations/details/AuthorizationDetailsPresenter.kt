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
    private val modelHasFinalMode: Boolean
        get() = currentViewModel?.hasFinalMode ?: false
    private var pollingService: SingleAuthorizationPollingService =
        apiManager.createSingleAuthorizationPollingService()
    private var authorizationId: String? = null
    private val authorizationAvailable: Boolean
        get() = currentViewModel?.viewMode != ViewMode.UNAVAILABLE
    private val viewMode: ViewMode
        get() = currentViewModel?.viewMode ?: ViewMode.LOADING

    fun setInitialData(connectionId: String?, authorizationId: String?) {
        super.currentConnectionAndKey = createConnectionAndKey(
            connectionID = connectionId ?: "",
            repository = connectionsRepository,
            keyStoreManager = keyStoreManager
        )
        this.authorizationId = authorizationId
        this.currentViewModel = AuthorizationViewModel(
            authorizationID = "",
            authorizationCode = "",
            title = "",
            description = "",
            validSeconds = 0,
            expiresAt = DateTime(0L),
            createdAt = DateTime(0L),
            connectionID = "",
            connectionName = "",
            connectionLogoUrl = "",
            viewMode = if (currentConnectionAndKey == null || authorizationId == null || authorizationId.isEmpty()) ViewMode.UNAVAILABLE else ViewMode.LOADING
        )
    }

    fun onFragmentResume() {
        if (modelCanBeRefreshed()) startPolling()
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
                model.shouldBeSetTimeOutMode -> {
                    stopPolling()
                    model.setNewViewMode(ViewMode.TIME_OUT)
                    viewContract?.updateTimeViews()
                    viewContract?.setContentViewMode(
                        viewMode,
                        ignoreTimeUpdate = viewMode.showProgress
                    )
                }
                model.shouldBeDestroyed -> {
                    viewContract?.closeView()
                }
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

    override fun onAuthorizeStart(
        connectionID: ConnectionID,
        authorizationID: AuthorizationID,
        type: ActionType
    ) {
        stopPolling()
        currentViewModel?.setNewViewMode(type.toViewMode())
        viewContract?.setContentViewMode(viewMode, ignoreTimeUpdate = viewMode.showProgress)
    }

    override fun onConfirmDenySuccess(
        success: Boolean,
        connectionID: ConnectionID,
        authorizationID: String
    ) {
        val newViewMode = if (success) {
            when (viewMode) {
                ViewMode.CONFIRM_PROCESSING -> ViewMode.CONFIRM_SUCCESS
                ViewMode.DENY_PROCESSING -> ViewMode.DENY_SUCCESS
                else -> ViewMode.ERROR
            }
        } else {
            startPolling()
            ViewMode.DEFAULT
        }
        currentViewModel?.setNewViewMode(newViewMode)
        viewContract?.setContentViewMode(newViewMode, ignoreTimeUpdate = newViewMode.showProgress)
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
                if (viewMode == ViewMode.LOADING) {
                    viewContract?.startTimer()
                }
                super.currentViewModel = newViewModel
                updateViewContent()
            }
        } ?: setUnavailableState()
    }

    private fun modelCanBeRefreshed(): Boolean {
        return viewMode === ViewMode.LOADING || viewMode === ViewMode.DEFAULT
    }

    private fun updateViewContent() {
        viewContract?.setHeaderVisibility(show = authorizationAvailable)
        viewContract?.setContentViewMode(viewMode, ignoreTimeUpdate = viewMode.showProgress)
        currentViewModel?.let {
            viewContract?.setHeaderValues(
                logoUrl = it.connectionLogoUrl ?: "",
                title = it.connectionName,
                startTime = it.createdAt,
                endTime = it.expiresAt
            )
            viewContract?.setContentTitleAndDescription(
                title = it.title,
                description = it.description
            )
        }
    }

    private fun processFetchAuthorizationError(error: ApiErrorData) {
        if (error.isConnectionNotFound()) {
            currentConnectionAndKey?.connection?.accessToken?.let {
                connectionsRepository.invalidateConnectionsByTokens(accessTokens = listOf(it))
            }
            currentViewModel?.setNewViewMode(ViewMode.ERROR)
            viewContract?.setContentViewMode(
                ViewMode.ERROR,
                ignoreTimeUpdate = ViewMode.ERROR.showProgress
            )
        } else if (shouldSetUnavailableState(error)) {
            setUnavailableState()
        } else if (!error.isConnectivityError()) {
            if (currentViewModel?.viewMode != ViewMode.ERROR) {
                viewContract?.showError(error.getErrorMessage(appContext))
                currentViewModel?.setNewViewMode(ViewMode.ERROR)
                stopPolling()
            }
        } else {
            viewContract?.showError(error.getErrorMessage(appContext))
        }
    }

    private fun shouldSetUnavailableState(error: ApiErrorData): Boolean {
        return error.isAuthorizationNotFound() && (viewMode === ViewMode.LOADING || viewMode === ViewMode.DEFAULT)
    }

    private fun setUnavailableState() {
        stopPolling()
        currentViewModel?.setNewViewMode(ViewMode.UNAVAILABLE)
        updateViewContent()
    }
}
