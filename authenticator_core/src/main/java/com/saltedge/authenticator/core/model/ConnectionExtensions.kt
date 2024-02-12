/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.model

/**
 * Check if connection is active
 *
 * @receiver connection
 * @return boolean, true if status == ACTIVE && has access_token
 */
fun ConnectionAbs.isActive(): Boolean {
    return this.getStatus() == ConnectionStatus.ACTIVE && this.accessToken.isNotEmpty()
}

/**
 * Get status of connection
 *
 * @receiver connection
 * @return connection status
 */
fun ConnectionAbs.getStatus(): ConnectionStatus {
    return this.status.toConnectionStatus() ?: ConnectionStatus.INACTIVE
}
