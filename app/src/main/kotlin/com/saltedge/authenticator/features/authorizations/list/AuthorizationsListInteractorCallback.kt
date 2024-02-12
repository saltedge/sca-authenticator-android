/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.authorizations.list

import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.AuthorizationStatus
import kotlinx.coroutines.CoroutineScope

interface AuthorizationsListInteractorCallback {
    fun onAuthorizationsReceived(data: List<AuthorizationItemViewModel>, newModelsApiVersion: String)
    fun onConfirmDenySuccess(connectionID: ID, authorizationID: ID, newStatus: AuthorizationStatus? = null)
    fun onConfirmDenyFailure(error: ApiErrorData, connectionID: ID, authorizationID: ID)
    val coroutineScope: CoroutineScope
}
