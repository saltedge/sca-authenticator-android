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
package com.saltedge.authenticator.features.authorizations.list

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.SHOW_REQUEST_CODE
import com.saltedge.authenticator.features.authorizations.common.*
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.KEY_ID
import com.saltedge.authenticator.sdk.contract.ConfirmAuthorizationResult
import com.saltedge.authenticator.sdk.contract.FetchAuthorizationsContract
import com.saltedge.authenticator.sdk.model.*
import com.saltedge.authenticator.sdk.tools.CryptoToolsAbs
import com.saltedge.authenticator.sdk.tools.KeyStoreManagerAbs
import com.saltedge.authenticator.tool.secure.fingerprint.BiometricToolsAbs
import javax.inject.Inject

class AuthorizationsListPresenter @Inject constructor(
    appContext: Context,
    private val cryptoTools: CryptoToolsAbs,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyStoreManagerAbs,
    biometricTools: BiometricToolsAbs,
    apiManager: AuthenticatorApiManagerAbs
) : BaseAuthorizationPresenter(appContext, biometricTools, apiManager),
    ListItemClickListener,
    FetchAuthorizationsContract,
    ConfirmAuthorizationResult {

    private var pollingService = apiManager.createAuthorizationsPollingService()
    private var connectionsAndKeys: Map<ConnectionID, ConnectionAndKey> =
        collectConnectionsAndKeys(connectionsRepository, keyStoreManager)
    var viewModels: List<AuthorizationViewModel> = emptyList()
    var viewContract: AuthorizationsListContract.View? = null

    override fun baseViewContract(): BaseAuthorizationViewContract? = viewContract

    fun onFragmentStart() {
        startPolling()
    }

    fun onFragmentStop() {
        stopPolling()
    }

    fun onRefreshClick() {
        pollingService.contract = this
        pollingService.forcedFetch()
    }

    fun onTimerTick() {
        if (existExpiredSessions()) {
            cleanDataSet()
            viewContract?.updateViewsContentInUiThread()
        } else viewContract?.refreshListView()
    }

    /**
     * Removing confirmed authorization from list
     * data are received from AuthorizationDetails
     */
    fun processFragmentResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null || resultCode != Activity.RESULT_OK) return
        val authorizationId = data.getStringExtra(KEY_ID) ?: return
        if (requestCode == SHOW_REQUEST_CODE) {
            viewModels = viewModels.filter { it.authorizationId != authorizationId }
            viewContract?.updateViewContent()
        }
    }

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        val viewModel = findAuthorizationById(itemCode) ?: return
        when (itemViewId) {
            R.id.positiveActionView, R.id.negativeActionView -> {
                super.currentViewModel = viewModel
                super.currentConnectionAndKey = connectionsAndKeys[viewModel.connectionId]
                onAuthorizeActionSelected(isConfirmed = itemViewId == R.id.positiveActionView)
            }
            R.id.detailsActionView -> {
                viewContract?.showAuthorizationDetailsView(authorizationViewModel = viewModel)
            }
        }
    }

    override fun getConnectionsData(): List<ConnectionAndKey>? =
        collectAuthorizationRequestData()

    override fun onFetchAuthorizationsResult(
        result: List<EncryptedAuthorizationData>,
        errors: List<ApiErrorData>
    ) {
        processAuthorizationsErrors(errors)
        processEncryptedAuthorizationsResult(result)
    }

    override fun onConfirmDenyFailure(error: ApiErrorData) {
        onConfirmActionFail(error)
    }

    override fun onConfirmDenySuccess(authorizationId: String, success: Boolean) {
        if (success) {
            this.viewModels = viewModels.filter { it.authorizationId != authorizationId }
            viewContract?.updateViewContent()
        }
        startPolling()
    }

    override fun updateConfirmProgressState(
        authorizationId: String?,
        confirmRequestIsInProgress: Boolean
    ) {
        if (confirmRequestIsInProgress) stopPolling() else startPolling()
        viewModels.find { it.authorizationId == authorizationId }?.let {
            it.isProcessing = confirmRequestIsInProgress
            viewContract?.updateItem(viewModel = it, itemId = viewModels.indexOf(it))
        }
    }

    private fun collectAuthorizationRequestData(): List<ConnectionAndKey>? {
        return if (connectionsAndKeys.isEmpty()) null else connectionsAndKeys.values.toList()
    }

    private fun processEncryptedAuthorizationsResult(result: List<EncryptedAuthorizationData>) {
        val newAuthorizationsData = decryptAuthorizations(result)
        val newViewModels = createViewModels(newAuthorizationsData)
        if (this.viewModels != newViewModels) {
            this.viewModels = newViewModels
            viewContract?.updateViewContent()
        }
    }

    private fun decryptAuthorizations(encryptedData: List<EncryptedAuthorizationData>): List<AuthorizationData> {
        return encryptedData.mapNotNull {
            cryptoTools.decryptAuthorizationData(
                encryptedData = it,
                rsaPrivateKey = connectionsAndKeys[it.connectionId]?.key
            )
        }.filter {
            it.isNotExpired()
        }.sortedBy { it.expiresAt }
    }

    private fun createViewModels(authorizations: List<AuthorizationData>): List<AuthorizationViewModel> {
        return authorizations.mapNotNull { item ->
            connectionsAndKeys[item.connectionId]?.let {
                item.toAuthorizationViewModel(it.connection)
            }
        }
    }

    private fun processAuthorizationsErrors(errors: List<ApiErrorData>) {
        val invalidTokens =
            errors.filter { it.isConnectionNotFound() }.mapNotNull { it.accessToken }
        if (invalidTokens.isNotEmpty()) {
            connectionsRepository.invalidateConnectionsByTokens(accessTokens = invalidTokens)
            connectionsAndKeys = collectConnectionsAndKeys(connectionsRepository, keyStoreManager)
        }
    }

    private fun cleanDataSet() {
        viewModels = viewModels.filter { it.isNotExpired() }
    }

    private fun existExpiredSessions(): Boolean = viewModels.any { it.isExpired() }

    private fun onConfirmActionFail(error: ApiErrorData) {
        startPolling()
        viewContract?.showError(error)
        viewContract?.reinitAndUpdateViewsContent(null)
    }

    private fun findAuthorizationById(id: String?) = viewModels.find { it.authorizationId == id }

    private fun startPolling() {
        pollingService.contract = this
        pollingService.start()
    }

    private fun stopPolling() {
        pollingService.contract = null
        pollingService.stop()
    }
}
