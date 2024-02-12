/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.model.authorization

import androidx.annotation.Keep
import com.saltedge.authenticator.core.model.ID
import java.io.Serializable

/**
 * Container for authorizationID and connectionID
 */
//TODO: Use where we use authorizationID with connectionID
@Keep
data class AuthorizationIdentifier(val authorizationID: ID, val connectionID: ID) : Serializable
