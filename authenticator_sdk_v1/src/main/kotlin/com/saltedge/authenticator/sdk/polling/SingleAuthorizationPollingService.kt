/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.polling

import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.polling.PollingServiceAbs
import com.saltedge.authenticator.sdk.api.RestClient
import com.saltedge.authenticator.sdk.api.connector.AuthorizationConnector
import com.saltedge.authenticator.sdk.contract.FetchAuthorizationListener
import timber.log.Timber

/**
 * Periodically query authorization
 *
 * @see PollingServiceAbs
 */
open class SingleAuthorizationPollingService : PollingServiceAbs<FetchAuthorizationContract>() {

    internal var connector: AuthorizationConnector? = null
    override var contract: FetchAuthorizationContract? = null
    private var authorizationID: String = ""

    fun start(authorizationID: String) {
        this.authorizationID = authorizationID
        connector = AuthorizationConnector(
            apiInterface = RestClient.apiInterface,
            resultCallback = contract
        )
        super.start()
    }

    override fun stop() {
        super.stop()
        connector = null
    }

    override fun forcedFetch() {
        try {
            contract?.getConnectionDataForAuthorizationPolling()?.let {
                connector?.getAuthorization(connectionAndKey = it, authorizationID = authorizationID)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}

interface FetchAuthorizationContract : FetchAuthorizationListener {
    fun getConnectionDataForAuthorizationPolling(): RichConnection?
}
