/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.model

enum class ConnectionStatus {
    INACTIVE, ACTIVE
}

/**
 * Convert status string to enum object or null if status unknown
 *
 * @receiver status string
 * @return ConnectionStatus, optional.
 */
fun String.toConnectionStatus(): ConnectionStatus? {
    return try {
        ConnectionStatus.valueOf(this)
    } catch (e: Exception) {
        null
    }
}
