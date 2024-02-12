/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.tools

import com.saltedge.authenticator.core.model.GUID
import java.util.*

const val MAX_GUID_LENGTH = 64

/**
 * Get random GUID string
 *
 * @return a string containing 64 characters
 */
fun createRandomGuid(): GUID =
    (UUID.randomUUID().toString() + UUID.randomUUID().toString()).take(MAX_GUID_LENGTH)
