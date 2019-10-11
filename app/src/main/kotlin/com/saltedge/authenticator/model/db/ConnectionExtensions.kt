/* 
 * This file is part of the Salt Edge Authenticator distribution 
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2019 Salt Edge Inc.
 * 
 * This program is free software: you can redistribute it and/or modify  
 * it under the terms of the GNU General Public License as published by  
 * the Free Software Foundation, version 3 or later.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * For the additional permissions granted for Salt Edge Authenticator
 * under Section 7 of the GNU General Public License see THIRD_PARTY_NOTICES.md
 */
package com.saltedge.authenticator.model.db

import com.saltedge.authenticator.sdk.model.*
import com.saltedge.authenticator.sdk.tools.KeyStoreManagerAbs
import com.saltedge.authenticator.tool.secure.createRandomBytesString
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.security.PrivateKey

/**
 * Creates Connection object with data from Provider data
 *
 * @receiver ProviderData object parsed from QR code
 * @return connection - filled Connection
 */
fun ProviderData.toConnection(): Connection? {
    if (!this.isValid()) return null
    val connection = Connection()
    connection.guid = createRandomBytesString()
    connection.name = this.name
    connection.code = this.code
    connection.logoUrl = this.logoUrl ?: ""
    connection.connectUrl = this.connectUrl
    connection.status = "${ConnectionStatus.INACTIVE}"
    connection.createdAt = DateTime.now().withZone(DateTimeZone.UTC).millis
    connection.updatedAt = connection.createdAt
    connection.supportEmail = this.supportEmail
    return connection
}

/**
 * Check if connection is active
 *
 * @receiver connection
 * @return boolean, true if status == ACTIVE && has access_token
 */
fun Connection.isActive(): Boolean {
    return this.getStatus() == ConnectionStatus.ACTIVE && this.accessToken.isNotEmpty()
}

/**
 * Get status of connection
 *
 * @receiver connection
 * @return connection status
 */
fun Connection.getStatus(): ConnectionStatus {
    return this.status.toConnectionStatus() ?: ConnectionStatus.INACTIVE
}

/**
 * Return pair of connection and private key from keystore
 *
 * @param keyStoreManager - key store manager
 * @see KeyStoreManagerAbs.getKeyPair
 * @receiver connection
 * @return ConnectionAndKey?
 */
fun Connection.toConnectionAndKey(keyStoreManager: KeyStoreManagerAbs):
    ConnectionAndKey? {
    return getRelatedPrivateKey(keyStoreManager)?.let {
        ConnectionAndKey(
            this as ConnectionAbs,
            it
        )
    }
}

/**
 * Get from KeyStore PrivateKey related to connection by it guid.
 *
 * @param keyStoreManager - Android key store
 * @receiver connection
 * @return private key
 */
fun ConnectionAbs.getRelatedPrivateKey(keyStoreManager: KeyStoreManagerAbs): PrivateKey? {
    return keyStoreManager.getKeyPair(this.guid)?.private
}
