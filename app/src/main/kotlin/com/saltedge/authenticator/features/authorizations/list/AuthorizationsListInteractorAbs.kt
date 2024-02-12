/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.authorizations.list

import com.saltedge.authenticator.core.model.ID

interface AuthorizationsListInteractorAbs {
    var contract: AuthorizationsListInteractorCallback?
    val noConnections: Boolean
    fun onResume()
    fun onStop()
    fun updateAuthorization(
        connectionID: ID,
        authorizationID: ID,
        authorizationCode: String,
        confirm: Boolean,
        locationDescription: String?
    ): Boolean
}
