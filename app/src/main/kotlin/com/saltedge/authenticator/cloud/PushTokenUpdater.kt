/*
 * Copyright (c) 2024 Salt Edge Inc.
 */
package com.saltedge.authenticator.cloud

import android.util.Log
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.contract.ConnectionUpdateListener
import com.saltedge.authenticator.models.toRichConnectionPair
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

open class PushTokenUpdater(
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyManagerAbs,
    private val apiManager: ScaServiceClientAbs,
    private val preferenceRepository: PreferenceRepositoryAbs,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main) // Внедрение зависимости для CoroutineScope

) : ConnectionUpdateListener {

    private var connections: List<Connection> = emptyList()
    private var richConnections: Map<ID, RichConnection> = emptyMap()

    fun updatePushToken() {
        connections = connectionsRepository.getActiveConnectionsWithoutToken(preferenceRepository.cloudMessagingToken)
        richConnections = connections.mapNotNull { it.toRichConnectionPair(keyStoreManager) }.toMap()
        connections.mapNotNull { connection -> richConnections[connection.id] }
            .forEach { richConnection ->
                apiManager.updatePushToken(
                    richConnection = richConnection,
                    currentPushToken = richConnection.connection.pushToken,
                    callback = this
                )
            }
    }

    override fun onUpdatePushTokenSuccess(connectionID: ID) {
        val connection = richConnections[connectionID]?.connection
        connection?.pushToken = preferenceRepository.cloudMessagingToken
        connectionsRepository.saveModel(connection as Connection)
    }

    override fun onUpdatePushTokenFailed(error: ApiErrorData) {
        Timber.e("onUpdatePushTokenFailed class: ${error.errorClassName}, message: ${error.errorMessage}")
    }
}
