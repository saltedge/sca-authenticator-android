/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.polling

import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.polling.PollingServiceAbs
import com.saltedge.authenticator.sdk.v2.api.connector.AuthorizationsIndexConnector
import com.saltedge.authenticator.sdk.v2.api.contract.FetchAuthorizationsListener
import com.saltedge.authenticator.sdk.v2.api.retrofit.RestClient

/**
 * Periodically query authorizations list.
 *
 * @see PollingServiceAbs
 */
class AuthorizationsPollingService : PollingServiceAbs<PollingAuthorizationsContract>() {

    override var contract: PollingAuthorizationsContract? = null

    override fun forcedFetch() {
        try {
            val connector = AuthorizationsIndexConnector(
                apiInterface = RestClient.apiInterface,
                callback = contract
            )
            contract?.getCurrentConnectionsAndKeysForPolling()?.let { connector.fetchActiveAuthorizations(connections = it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

interface PollingAuthorizationsContract : FetchAuthorizationsListener {
    fun getCurrentConnectionsAndKeysForPolling(): List<RichConnection>?
}
