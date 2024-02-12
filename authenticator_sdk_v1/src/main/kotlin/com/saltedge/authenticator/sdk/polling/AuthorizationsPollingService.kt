/*
 * Copyright (c) 2019 Salt Edge Inc.
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
