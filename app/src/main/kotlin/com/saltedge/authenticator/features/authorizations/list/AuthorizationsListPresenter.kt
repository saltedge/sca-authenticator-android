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

import android.content.Context
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.authorizations.common.*
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.contract.ConfirmAuthorizationResult
import com.saltedge.authenticator.sdk.contract.FetchAuthorizationsContract
import com.saltedge.authenticator.sdk.model.*
import com.saltedge.authenticator.sdk.tools.crypt.CryptoToolsAbs
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.sdk.tools.biometric.BiometricToolsAbs
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import com.saltedge.authenticator.sdk.model.ConnectionAndKey

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
    ConfirmAuthorizationResult,
    AuthorizationStatusListener,
    CoroutineScope
{
    private val job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    var viewModels: ObservableList<AuthorizationViewModel> =
        ObservableArrayList<AuthorizationViewModel>()
    var showBiometricConfirmation: Boolean = false
    var viewContract: AuthorizationsListContract.View? = null
    val showEmptyView: Boolean
        get() = viewModels.isEmpty()
    val showContentViews: Boolean
        get() = !showEmptyView
    private var pollingService = apiManager.createAuthorizationsPollingService()
    private var connectionsAndKeys: Map<ConnectionID, ConnectionAndKey> =
        collectConnectionsAndKeys(connectionsRepository, keyStoreManager)
    private val authorizingInProgress: Boolean
        get() = viewModels.any { it.viewMode == ViewMode.CONFIRM_PROCESSING || it.viewMode == ViewMode.DENY_PROCESSING }

    override fun baseViewContract(): BaseAuthorizationViewContract? = viewContract

    fun onFragmentResume() {
        startPolling()
        viewContract?.updateViewsContent()
    }

    fun onFragmentPause() {
        stopPolling()
    }

    fun onFragmentDestroy() {
        job.cancel()
    }

    override fun onViewModelsExpired() {
        val expiredViewModels = viewModels.filter { it.shouldBeSetTimeOutMode }
        if (expiredViewModels.isNotEmpty()) {
            expiredViewModels.forEach { it.setNewViewMode(ViewMode.TIME_OUT) }
            viewContract?.updateViewsContent()
        }
    }

    override fun onViewModelsShouldBeDestroyed() {
        val filteredList = viewModels.filter { !it.shouldBeDestroyed }
        viewModels.replaceWith(filteredList)
        viewContract?.updateViewsContent()
    }

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        val viewModel = viewModels.getOrNull(itemIndex) ?: return
        when (itemViewId) {
            R.id.positiveActionView -> {
                onListItemClick(viewModel, selectedActionType = ActionType.CONFIRM)
            }
            R.id.negativeActionView -> {
                onListItemClick(viewModel, selectedActionType = ActionType.DENY)
            }
        }
    }

    override fun getConnectionsData(): List<ConnectionAndKey>? = collectAuthorizationRequestData()

    override fun onFetchAuthorizationsResult(
        result: List<EncryptedAuthorizationData>,
        errors: List<ApiErrorData>
    ) {
        processAuthorizationsErrors(errors)
        processEncryptedAuthorizationsResult(result)
    }

    override fun onAuthorizeStart(
        connectionID: ConnectionID,
        authorizationID: AuthorizationID,
        type: ActionType
    ) {
        stopPolling()
        findViewModel(connectionID, authorizationID)?.let { viewModel ->
            viewModel.setNewViewMode(type.toViewMode())
            viewContract?.updateItem(viewModel = viewModel, itemId = viewModels.indexOf(viewModel))
        }
    }

    override fun onConfirmDenySuccess(
        success: Boolean,
        connectionID: ConnectionID,
        authorizationID: AuthorizationID
    ) {
        findViewModel(connectionID, authorizationID)?.let { viewModel ->
            viewModel.setNewViewMode(if (viewModel.viewMode == ViewMode.DENY_PROCESSING) ViewMode.DENY_SUCCESS else ViewMode.CONFIRM_SUCCESS)
            viewContract?.updateItem(viewModel = viewModel, itemId = viewModels.indexOf(viewModel))
        }
        startPolling()
    }

    override fun onConfirmDenyFailure(
        error: ApiErrorData,
        connectionID: ConnectionID,
        authorizationID: AuthorizationID
    ) {
        viewContract?.showError(error)
        findViewModel(connectionID, authorizationID)?.let { viewModel ->
            viewModel.setNewViewMode(ViewMode.ERROR)
            viewContract?.updateItem(viewModel = viewModel, itemId = viewModels.indexOf(viewModel))
        }
        startPolling()
    }

    private fun onListItemClick(viewModel: AuthorizationViewModel, selectedActionType: ActionType) {
        if (!authorizingInProgress) {
            super.currentViewModel = viewModel
            super.currentConnectionAndKey = connectionsAndKeys[viewModel.connectionID] ?: return
            onAuthorizeActionSelected(
                requestType = selectedActionType,
                showBiometricConfirmation = showBiometricConfirmation
            )
        }
    }

    private fun collectAuthorizationRequestData(): List<ConnectionAndKey>? {
        return if (connectionsAndKeys.isEmpty()) null else connectionsAndKeys.values.toList()
    }

    private fun processEncryptedAuthorizationsResult(result: List<EncryptedAuthorizationData>) {
        launch {
            val data = decryptAuthorizations(result)
            withContext(Dispatchers.Main) {
                processDecryptedAuthorizationsResult(result = data)
            }
        }
    }

    private fun decryptAuthorizations(encryptedData: List<EncryptedAuthorizationData>): List<AuthorizationData> {
        return encryptedData.mapNotNull {
            cryptoTools.decryptAuthorizationData(
                encryptedData = it,
                rsaPrivateKey = connectionsAndKeys[it.connectionId]?.key
            )
        }
    }

    //TODO SET AS PRIVATE AFTER CREATING TEST FOR COROUTINE
    fun processDecryptedAuthorizationsResult(result: List<AuthorizationData>) {
        val newAuthorizationsData = result
            .filter { it.isNotExpired() }
            .sortedBy { it.createdAt }
        val joinedViewModels = createViewModels(newAuthorizationsData).joinFinalModels(this.viewModels)
        if (this.viewModels != joinedViewModels) {
            this.viewModels.replaceWith(joinedViewModels)
            viewContract?.updateViewsContent()
        }
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

    private fun findViewModel(
        connectionID: ConnectionID,
        authorizationID: AuthorizationID
    ): AuthorizationViewModel? {
        return viewModels.find { it.authorizationID == authorizationID && it.connectionID == connectionID }
    }

    private fun startPolling() {
        pollingService.contract = this
        pollingService.start()
    }

    private fun stopPolling() {
        pollingService.contract = null
        pollingService.stop()
    }

    fun setOnDataChange() {
        viewModels.addOnListChangedCallback(object :
            ObservableList.OnListChangedCallback<ObservableList<AuthorizationViewModel>>() {
            override fun onChanged(sender: ObservableList<AuthorizationViewModel>?) {}

            override fun onItemRangeRemoved(
                sender: ObservableList<AuthorizationViewModel>?,
                positionStart: Int,
                itemCount: Int
            ) {
                showBiometricConfirmation = showConfirmation(itemCount)
            }

            override fun onItemRangeMoved(
                sender: ObservableList<AuthorizationViewModel>?,
                fromPosition: Int,
                toPosition: Int,
                itemCount: Int
            ) {}

            override fun onItemRangeInserted(
                sender: ObservableList<AuthorizationViewModel>?,
                positionStart: Int,
                itemCount: Int
            ) {
                showBiometricConfirmation = showConfirmation(itemCount)
            }

            override fun onItemRangeChanged(
                sender: ObservableList<AuthorizationViewModel>?,
                positionStart: Int,
                itemCount: Int
            ) {}
        })
    }

    private fun showConfirmation(itemCount: Int): Boolean {
        return itemCount != 1
    }
}
