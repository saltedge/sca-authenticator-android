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

import android.view.View
import com.saltedge.authenticator.sdk.model.AuthorizationID
import com.saltedge.authenticator.sdk.model.ConnectionID
import com.saltedge.authenticator.sdk.model.authorization.AuthorizationData
import com.saltedge.authenticator.sdk.model.connection.ConnectionAbs
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
    val endTime: DateTime,
    val startTime: DateTime,
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
     * Check that `time views` should be hidden
     *
     * @return View.INVISIBLE if view mode is UNAVAILABLE or LOADING or View.VISIBLE
     */
    val timeViewVisibility: Int
        get() {
            val invisible = viewMode == ViewMode.UNAVAILABLE || viewMode == ViewMode.LOADING
            return if (invisible) View.INVISIBLE else View.VISIBLE
        }

    /**
     * Check that `time views` should ignore time changes.
     * freeze last time state
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
        get() = endTime.isEqualNow || endTime.isBeforeNow

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
        get() = endTime.remainedSeconds()

    /**
     * Calculates interval between current time and expiresAt time and prepare timestamp string result
     *
     * @return String, timestamp in "minutes:seconds" format
     */
    val remainedTimeStringTillExpire: String
        get() = endTime.remainedSeconds().remainedTimeDescription()
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
        endTime = this.expiresAt,
        startTime = this.createdAt ?: DateTime(0L),
        authorizationID = this.id,
        authorizationCode = this.authorizationCode ?: ""
    )
}

/**
 * Converts AuthorizationData in AuthorizationViewModel
 *
 * @param newViewModels newly received list of AuthorizationViewModel's
 * @param oldViewModels previously stored list of AuthorizationViewModel's
 * @return List, result of merging
 */
fun joinViewModels(newViewModels: List<AuthorizationViewModel>, oldViewModels: List<AuthorizationViewModel>): List<AuthorizationViewModel> {
    val finalModels = oldViewModels.filter { it.hasFinalMode }
    val newModelsWithoutExitingFinalModels = newViewModels.filter {
        !finalModels.containsIdentifier(it.authorizationID, it.connectionID)
    }
    return (newModelsWithoutExitingFinalModels + finalModels).sortedBy { it.startTime }
}

private fun List<AuthorizationViewModel>.containsIdentifier(
    authorizationID: AuthorizationID,
    connectionID: ConnectionID): Boolean
{
    return this.any { it.hasIdentifier(authorizationID, connectionID) }
}

private fun AuthorizationViewModel.hasIdentifier(authorizationID: AuthorizationID, connectionID: ConnectionID): Boolean {
    return this.authorizationID == authorizationID && this.connectionID == connectionID
}

private fun authorizationExpirationPeriod(authorization: AuthorizationData): Int {
    return secondsBetweenDates(authorization.createdAt ?: return 0, authorization.expiresAt)
}
