/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.model

import java.security.PrivateKey
import java.security.PublicKey

/**
 * Container for Connection model and related PrivateKey, PublicKey
 */
data class RichConnection(
    val connection: ConnectionAbs,
    val private: PrivateKey,
    val providerPublic: PublicKey? = null
)
