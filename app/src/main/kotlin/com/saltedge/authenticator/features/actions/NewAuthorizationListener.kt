/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.actions

import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationIdentifier

interface NewAuthorizationListener {
    fun onNewAuthorization(authorizationIdentifier: AuthorizationIdentifier)
}

