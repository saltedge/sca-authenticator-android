/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.polling

import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.polling.PollingServiceAbs
import com.saltedge.authenticator.sdk.v2.api.connector.AuthorizationShowConnector
import com.saltedge.authenticator.sdk.v2.api.contract.FetchAuthorizationListener
import com.saltedge.authenticator.sdk.v2.api.retrofit.RestClient

/**
 * Periodically query authorization
 *
 * @see PollingServiceAbs
 */
open class SingleAuthorizationPollingService : PollingServiceAbs<PollingAuthorizationContract>() {

    internal var connector: AuthorizationShowConnector? = null
    override var contract: PollingAuthorizationContract? = null
    private var authorizationID: String = ""

    fun start(authorizationID: String) {
        this.authorizationID = authorizationID
        connector = AuthorizationShowConnector(
            apiInterface = RestClient.apiInterface,
            callback = contract
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
                connector?.showAuthorization(
                    connection = it.connection,
                    authorizationId = authorizationID
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

interface PollingAuthorizationContract : FetchAuthorizationListener {
    fun getConnectionDataForAuthorizationPolling(): RichConnection?
}
