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
package com.saltedge.authenticator.sdk.polling

import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.polling.PollingServiceAbs
import com.saltedge.authenticator.sdk.api.RestClient
import com.saltedge.authenticator.sdk.api.connector.AuthorizationsConnector
import com.saltedge.authenticator.sdk.contract.FetchEncryptedDataListener
import timber.log.Timber

/**
 * Periodically query authorizations list.
 *
 * @see PollingServiceAbs
 */
class AuthorizationsPollingService : PollingServiceAbs<FetchAuthorizationsContract>() {

    override var contract: FetchAuthorizationsContract? = null

    override fun forcedFetch() {
        try {
            val connector = AuthorizationsConnector(
                apiInterface = RestClient.apiInterface,
                resultCallback = contract
            )
            contract?.getCurrentConnectionsAndKeysForPolling()?.let { connector.fetchAuthorizations(connectionsAndKeys = it) }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}

interface FetchAuthorizationsContract : FetchEncryptedDataListener {
    fun getCurrentConnectionsAndKeysForPolling(): List<RichConnection>?
}
