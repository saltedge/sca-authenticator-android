/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.model

import java.util.*

enum class ConsentType {
    AISP, PISP_FUTURE, PISP_RECURRING;
}

/**
 * Convert type string to enum object or null if unknown
 *
 * @receiver type string
 * @return ConsentType, optional.
 */
fun String.toConsentType(): ConsentType? {
    return try {
        ConsentType.valueOf(this.uppercase(Locale.US))
    } catch (e: Exception) {
        null
    }
}
