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
package com.saltedge.authenticator.features.authorizations.common

import com.saltedge.authenticator.sdk.model.AuthorizationData
import com.saltedge.authenticator.sdk.model.AuthorizationID
import com.saltedge.authenticator.sdk.model.ConnectionAbs
import com.saltedge.authenticator.sdk.model.ConnectionID
import com.saltedge.authenticator.sdk.tools.remainedExpirationTime
import com.saltedge.authenticator.sdk.tools.remainedSecondsTillExpire
import com.saltedge.authenticator.sdk.tools.secondsBetweenDates
import org.joda.time.DateTime
import java.io.Serializable

data class AuthorizationViewModel(
        val authorizationId: AuthorizationID,
        val authorizationCode: String,
        val title: String,
        val description: String,
        val validSeconds: Int,
        val expiresAt: DateTime,
        val connectionId: ConnectionID,
        val connectionName: String,
        var connectionLogoUrl: String?,
        var isProcessing: Boolean
) : Serializable

/**
 * Check what authorization is expired
 *
 * @receiver authorization view model
 * @return true if expiresAt time is before now
 */
fun AuthorizationViewModel.isExpired(): Boolean = expiresAt.isBeforeNow

/**
 * Check what authorization is not expired
 *
 * @receiver authorization view model
 * @return true if expiresAt time is after now
 */
fun AuthorizationViewModel.isNotExpired(): Boolean = expiresAt.isAfterNow

/**
 * Calculates interval (count of seconds) between current time and expiresAt time
 *
 * @receiver authorization view model
 * @return integer, seconds
 */
fun AuthorizationViewModel.remainedSecondsTillExpire(): Int = expiresAt.remainedSecondsTillExpire()

/**
 * Calculates interval between current time and expiresAt time and prepare timestamp string result
 *
 * @receiver authorization view model
 * @return timestamp string
 */
fun AuthorizationViewModel.remainedTimeTillExpire(): String = expiresAt.remainedExpirationTime()

/**
 * Converts AuthorizationData in AuthorizationViewModel
 *
 * @receiver Authorization
 * @param connection - Connection
 * @return Authorization view model
 */
fun AuthorizationData.toAuthorizationViewModel(connection: ConnectionAbs): AuthorizationViewModel {
    return AuthorizationViewModel(
            title = this.title,
            description = this.description,
            connectionId = connection.id,
            connectionName = connection.name,
            connectionLogoUrl = connection.logoUrl,
            validSeconds = authorizationExpirationPeriod(this),
            expiresAt = this.expiresAt,
            authorizationId = this.id,
            authorizationCode = this.authorizationCode ?: "",
            isProcessing = false)
}

/**
 * Calculates period (seconds) between current time and expiresAt time
 *
 * @receiver authorization
 * @return integer, seconds
 */
private fun authorizationExpirationPeriod(authorization: AuthorizationData): Int {
    return secondsBetweenDates(authorization.createdAt ?: return 0, authorization.expiresAt)
}
