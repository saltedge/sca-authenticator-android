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

import com.saltedge.authenticator.sdk.model.authorization.AuthorizationData
import com.saltedge.authenticator.sdk.model.AuthorizationID
import com.saltedge.authenticator.sdk.model.connection.ConnectionAbs
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

    /**
     * Set new viewMode and if is final mode set destroyAt
     */
    fun setNewViewMode(newViewMode: ViewMode) {
        this.viewMode = newViewMode
        this.destroyAt = if (newViewMode.isFinalMode()) DateTime.now().plusSeconds(3) else null
    }

    /**
     * Check that should be set viewMode == TIME_OUT
     *
     * @return Boolean, true if model is expired and does not have final viewMode
     */
    val shouldBeSetTimeOutMode: Boolean
        get() = this.isExpired && !this.hasFinalMode && this.viewMode != ViewMode.LOADING

    /**
     * Check that model can be authorized
     *
     * @return Boolean, true viewMode is default mode
     */
    val canBeAuthorized: Boolean
        get() = viewMode === ViewMode.DEFAULT

    /**
     * Check that model has final viewMode
     *
     * @return Boolean, true viewMode is final mode
     */
    val hasFinalMode: Boolean
        get() = viewMode.isFinalMode()

    /**
     * Check that `time views` should ignore time changes
     *
     * @return Boolean, true if view mode is not default
     */
    val ignoreTimeUpdate: Boolean
        get() = viewMode !== ViewMode.DEFAULT

    /**
     * Check that authorization model should be destroyed
     *
     * @return Boolean, true if destroyAt time is before now
     */
    val shouldBeDestroyed: Boolean
        get() = destroyAt?.isBeforeNow == true

    /**
     * Check that authorization is expired
     *
     * @return Boolean, true if expiresAt time is before now
     */
    val isExpired: Boolean
        get() = expiresAt.isEqualNow || expiresAt.isBeforeNow

    /**
     * Check that authorization is not expired
     *
     * @return Boolean, true if expiresAt time is after now
     */
    val isNotExpired: Boolean
        get() = !isExpired

    /**
     * Calculates interval (count of seconds) between current time and expiresAt time
     *
     * @return Int, seconds
     */
    val remainedSecondsTillExpire: Int
        get() = expiresAt.remainedSeconds()

    /**
     * Calculates interval between current time and expiresAt time and prepare timestamp string result
     *
     * @return String, timestamp in "minutes:seconds" format
     */
    val remainedTimeStringTillExpire: String
        get() = expiresAt.remainedSeconds().remainedTimeDescription()
}

/**
 * Converts AuthorizationData in AuthorizationViewModel
 *
 * @receiver Authorization
 * @param connection - Connection
 * @return AuthorizationViewModel
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

/**
 * Converts AuthorizationData in AuthorizationViewModel
 *
 * @receiver list of AuthorizationViewModel's received from server
 * @param listWithFinalModels stored in presenter list of AuthorizationViewModel's
 * @return List, result of merging
 */
fun List<AuthorizationViewModel>.joinFinalModels(listWithFinalModels: List<AuthorizationViewModel>): List<AuthorizationViewModel> {
    val finalModels = listWithFinalModels.filter { it.hasFinalMode }
    val finalAuthorizationIDs = finalModels.map { it.authorizationID }
    val finalConnectionIDs = finalModels.map { it.connectionID }
    return (this.filter {
        !finalAuthorizationIDs.contains(it.authorizationID) || !finalConnectionIDs.contains(it.connectionID)
    } + finalModels).sortedBy { it.createdAt }
}

private fun authorizationExpirationPeriod(authorization: AuthorizationData): Int {
    return secondsBetweenDates(authorization.createdAt ?: return 0, authorization.expiresAt)
}
