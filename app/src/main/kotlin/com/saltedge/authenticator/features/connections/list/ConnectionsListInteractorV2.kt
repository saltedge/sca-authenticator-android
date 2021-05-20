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
package com.saltedge.authenticator.features.connections.list

import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.tools.CryptoToolsAbs
import com.saltedge.authenticator.sdk.v2.ScaServiceClient
import com.saltedge.authenticator.sdk.v2.api.contract.ConnectionsRevokeListener

class ConnectionsListInteractorV2(
    private val apiManager: ScaServiceClient,
    connectionsRepository: ConnectionsRepositoryAbs,
    keyStoreManager: KeyManagerAbs,
    cryptoTools: CryptoToolsAbs
) : ConnectionsListInteractor(
    connectionsRepository = connectionsRepository,
    keyStoreManager = keyStoreManager,
    cryptoTools = cryptoTools
), ConnectionsRevokeListener {

    override fun revokeConnections(connectionsAndKeys: List<RichConnection>) {
        apiManager.revokeConnections(
            connections = connectionsAndKeys,
            callback = this
        )
    }

    override fun onConnectionsRevokeResult(apiError: ApiErrorData?) {}
}
