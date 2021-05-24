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
package com.saltedge.authenticator.features.authorizations.details

import androidx.lifecycle.Lifecycle
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.isAuthorizationNotFound
import com.saltedge.authenticator.core.api.model.error.isConnectionNotFound
import com.saltedge.authenticator.core.api.model.error.isConnectivityError
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.createRichConnection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs

abstract class AuthorizationDetailsInteractor(
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyManagerAbs,
) : AuthorizationDetailsInteractorAbs {
    override var contract: AuthorizationDetailsInteractorCallback? = null
    var richConnection: RichConnection? = null
        private set
    override val noConnection: Boolean
        get() = richConnection == null
    override val connectionApiVersion: String?
        get() = richConnection?.connection?.apiVersion

    override fun setInitialData(connectionID: ID) {
        richConnection = createRichConnection(
            connectionID = connectionID,
            repository = connectionsRepository,
            keyStoreManager = keyStoreManager
        )
    }

    protected fun processApiError(error: ApiErrorData) {
        when {
            error.isConnectionNotFound() -> {
                richConnection?.connection?.accessToken?.let {
                    connectionsRepository.invalidateConnectionsByTokens(accessTokens = listOf(it))
                }
                stopPolling()
                contract?.onConnectionNotFoundError()
            }
            error.isAuthorizationNotFound() -> {
                stopPolling()
                contract?.onAuthorizationNotFoundError()
            }
            error.isConnectivityError() -> contract?.onConnectivityError(error)
            else -> contract?.onError(error)
        }
    }

    abstract override fun stopPolling()
}

interface AuthorizationDetailsInteractorAbs {
    var contract: AuthorizationDetailsInteractorCallback?
    val connectionApiVersion: String?
    val noConnection: Boolean
    fun bindLifecycleObserver(lifecycle: Lifecycle)
    fun setInitialData(connectionID: ID)
    fun startPolling(authorizationID: ID)
    fun stopPolling()
    fun updateAuthorization(authorizationID: ID, authorizationCode: String, confirm: Boolean): Boolean
}
