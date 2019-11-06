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
package com.saltedge.authenticator.model

import com.saltedge.authenticator.model.db.Connection
import com.saltedge.authenticator.sdk.model.ConnectionStatus
import com.saltedge.authenticator.sdk.model.ProviderData
import com.saltedge.authenticator.sdk.model.isValid
import com.saltedge.authenticator.sdk.tools.createRandomGuid
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

/**
 * Creates Connection object with data from Provider data
 *
 * @receiver ProviderData object parsed from QR code
 * @return connection - filled Connection
 */
fun ProviderData.toConnection(): Connection? {
    if (!this.isValid()) return null
    val connection = Connection()
    connection.guid = createRandomGuid()
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
