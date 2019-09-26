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
import com.saltedge.authenticator.sdk.tools.remainedSeconds
import com.saltedge.authenticator.sdk.tools.remainedTimeDescription
import com.saltedge.authenticator.sdk.tools.secondsBetweenDates
import org.joda.time.DateTime
import java.io.Serializable

data class AuthorizationViewModel(
    val authorizationID: AuthorizationID,
    val authorizationCode: String,
    val title: String,
    val description: String,
    val validSeconds: Int,
    val expiresAt: DateTime,
    val createdAt: DateTime,
    val connectionID: ConnectionID,
    val connectionName: String,
    val connectionLogoUrl: String?,
    var viewMode: ViewMode = ViewMode.DEFAULT
) : Serializable {

    var destroyAt: DateTime? = null

    fun setNewViewMode(newViewMode: ViewMode) {
        this.viewMode = newViewMode
        this.destroyAt = if (newViewMode.isFinalMode()) DateTime.now().plusSeconds(3) else null
    }

    val shouldBeSetTimeOutMode: Boolean
        get() = this.isExpired && !this.hasFinalMode

    val hasFinalMode: Boolean
        get() = viewMode.isFinalMode()

    val ignoreTimeUpdate: Boolean
        get() = viewMode.showProgress

    /**
     * Check what authorization model should be destroyed
     *
     * @receiver authorization view model
     * @return true if destroyAt time is before now
     */
    val shouldBeDestroyed: Boolean
        get() = destroyAt?.isBeforeNow == true

    /**
     * Check what authorization is expired
     *
     * @receiver authorization view model
     * @return true if expiresAt time is before now
     */
    val isExpired: Boolean
        get() = expiresAt.isEqualNow || expiresAt.isBeforeNow

    /**
     * Check what authorization is not expired
     *
     * @receiver authorization view model
     * @return true if expiresAt time is after now
     */
    val isNotExpired: Boolean
        get() = !isExpired

    /**
     * Calculates interval (count of seconds) between current time and expiresAt time
     *
     * @receiver authorization view model
     * @return integer, seconds
     */
    val remainedSecondsTillExpire: Int
        get() = expiresAt.remainedSeconds()

    /**
     * Calculates interval between current time and expiresAt time and prepare timestamp string result
     *
     * @receiver authorization view model
     * @return timestamp string in "minutes:seconds" format
     */
    val remainedTimeStringTillExpire: String
        get() = expiresAt.remainedSeconds().remainedTimeDescription()
}

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
        connectionID = connection.id,
        connectionName = connection.name,
        connectionLogoUrl = connection.logoUrl,
        validSeconds = authorizationExpirationPeriod(this),
        expiresAt = this.expiresAt,
        createdAt = this.createdAt ?: DateTime(0L),
        authorizationID = this.id,
        authorizationCode = this.authorizationCode ?: ""
    )
}

fun List<AuthorizationViewModel>.joinFinalModels(listWithFinalModels: List<AuthorizationViewModel>): List<AuthorizationViewModel> {
    val finalModels = listWithFinalModels.filter { it.hasFinalMode }
    val finalAuthorizationIDs = finalModels.map { it.authorizationID }
    val finalConnectionIDs = finalModels.map { it.connectionID }
    return (this.filter {
        !finalAuthorizationIDs.contains(it.authorizationID) || !finalConnectionIDs.contains(it.connectionID)
    } + finalModels).sortedBy { it.createdAt }
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
