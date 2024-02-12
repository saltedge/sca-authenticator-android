/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.model.authorization

import androidx.annotation.Keep
import java.io.Serializable

/**
 * Container for authorizationID and connectionID
 */
//TODO: Use where we use authorizationID with connectionID
@Keep
data class AuthorizationIdentifier(val authorizationID: String, val connectionID: String) : Serializable {
    val hasAuthorizationID: Boolean
        get() = authorizationID.isNotEmpty()
}
