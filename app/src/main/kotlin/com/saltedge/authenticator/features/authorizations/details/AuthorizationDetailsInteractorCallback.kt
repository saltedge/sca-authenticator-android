/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.authorizations.details

import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.AuthorizationStatus
import kotlinx.coroutines.CoroutineScope

interface AuthorizationDetailsInteractorCallback {
    fun onAuthorizationReceived(data: AuthorizationItemViewModel?, newModelApiVersion: String)
    fun onConfirmDenySuccess(newStatus: AuthorizationStatus? = null)
    fun onConnectionNotFoundError()
    fun onAuthorizationNotFoundError()
    fun onConnectivityError(error: ApiErrorData)
    fun onError(error: ApiErrorData)

    val coroutineScope: CoroutineScope
}
