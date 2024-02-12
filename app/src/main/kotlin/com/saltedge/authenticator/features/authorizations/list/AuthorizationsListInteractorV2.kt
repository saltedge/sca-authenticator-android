/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.authorizations.list

import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.core.api.model.DescriptionData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.isConnectionNotFound
import com.saltedge.authenticator.core.api.model.error.isConnectionRevoked
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.LIFE_TIME_OF_FINAL_MODEL
import com.saltedge.authenticator.features.authorizations.common.isClosed
import com.saltedge.authenticator.features.authorizations.common.isFinalStatus
import com.saltedge.authenticator.features.authorizations.common.toAuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.toAuthorizationStatus
import com.saltedge.authenticator.models.collectRichConnections
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import com.saltedge.authenticator.sdk.v2.api.contract.AuthorizationConfirmListener
import com.saltedge.authenticator.sdk.v2.api.contract.AuthorizationDenyListener
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationResponseData
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationV2Data
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationData
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationResponseData
import com.saltedge.authenticator.sdk.v2.api.model.authorization.isNotExpired
import com.saltedge.authenticator.sdk.v2.polling.PollingAuthorizationsContract
import com.saltedge.authenticator.sdk.v2.tools.CryptoToolsV2Abs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime

class AuthorizationsListInteractorV2(
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyManagerAbs,
    private val cryptoTools: CryptoToolsV2Abs,
    private val apiManager: ScaServiceClientAbs,
    private val defaultDispatcher: CoroutineDispatcher,
) : AuthorizationsListInteractorAbs,
    AuthorizationConfirmListener,
    AuthorizationDenyListener,
    PollingAuthorizationsContract
{
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

    override fun getCurrentConnectionsAndKeysForPolling(): List<RichConnection> =
        richConnections.values.toList()

    override fun onFetchAuthorizationsResult(
        result: List<AuthorizationResponseData>,
        errors: List<ApiErrorData>
    ) {
        processAuthorizationsErrors(errors = errors)
        processEncryptedAuthorizationsResult(encryptedList = result)
    }

    override fun onAuthorizationConfirmSuccess(
        result: UpdateAuthorizationResponseData,
        connectionID: ID
    ) {
        contract?.onConfirmDenySuccess(
            connectionID = connectionID,
            authorizationID = result.authorizationID,
            newStatus = result.status.toAuthorizationStatus()
        )
    }

    override fun onAuthorizationConfirmFailure(
        error: ApiErrorData,
        connectionID: ID,
        authorizationID: ID
    ) {
        contract?.onConfirmDenyFailure(
            error = error,
            connectionID = connectionID,
            authorizationID = authorizationID
        )
    }

    override fun onAuthorizationDenySuccess(
        result: UpdateAuthorizationResponseData,
        connectionID: ID
    ) {
        contract?.onConfirmDenySuccess(
            connectionID = connectionID,
            authorizationID = result.authorizationID,
            newStatus = result.status.toAuthorizationStatus()
        )
    }

    override fun onAuthorizationDenyFailure(
        error: ApiErrorData,
        connectionID: ID,
        authorizationID: ID
    ) {
        contract?.onConfirmDenyFailure(
            error = error,
            connectionID = connectionID,
            authorizationID = authorizationID
        )
    }

    private fun processAuthorizationsErrors(errors: List<ApiErrorData>) {
        val invalidTokens = errors.filter { it.isConnectionNotFound() || it.isConnectionRevoked() }
            .mapNotNull { it.accessToken }
        if (invalidTokens.isNotEmpty()) {
            connectionsRepository.invalidateConnectionsByTokens(accessTokens = invalidTokens)
            richConnections = collectRichConnections()
        }
    }

    private fun processEncryptedAuthorizationsResult(encryptedList: List<AuthorizationResponseData>) {
        contract?.coroutineScope?.launch(defaultDispatcher) {
            val splitList: Pair<List<AuthorizationResponseData>, List<AuthorizationResponseData>> =
                encryptedList.filterNot { it.status.isClosed }.partition { it.status.isFinalStatus }
            val finishedData: List<AuthorizationV2Data> = prepareFinishedAuthorizationData(splitList.first)
            val activeData: List<AuthorizationV2Data> = decryptAuthorizations(splitList.second)
            val items: List<AuthorizationItemViewModel> = createViewModels((activeData.filter { it.isNotExpired() } + finishedData))

            withContext(Dispatchers.Main) {
                contract?.onAuthorizationsReceived(data = items, newModelsApiVersion = API_V2_VERSION)
            }
        }
    }

    private fun prepareFinishedAuthorizationData(dataList: List<AuthorizationResponseData>): List<AuthorizationV2Data> {
        return dataList.mapNotNull { data ->
            data.finishedAt?.let { finishedAt ->
                if (finishedAt.plusSeconds(LIFE_TIME_OF_FINAL_MODEL).isBeforeNow) {
                    null
                } else {
                    AuthorizationV2Data(
                        connectionID = data.connectionId,
                        authorizationID = data.id,
                        status = data.status,
                        description = DescriptionData(text = null),
                        expiresAt = DateTime(0),
                        title = "",
                        finishedAt = finishedAt
                    )
                }
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
