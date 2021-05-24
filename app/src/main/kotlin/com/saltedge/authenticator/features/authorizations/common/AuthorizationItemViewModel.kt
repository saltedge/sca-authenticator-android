/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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
import com.saltedge.authenticator.core.api.model.DescriptionData
import com.saltedge.authenticator.core.api.model.DescriptionHTMLData
import com.saltedge.authenticator.core.api.model.DescriptionTextData
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.tools.hasHTMLTags
import com.saltedge.authenticator.core.tools.remainedSeconds
import com.saltedge.authenticator.core.tools.remainedTimeDescription
import com.saltedge.authenticator.core.tools.secondsBetweenDates
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationData
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationV2Data
import org.joda.time.DateTime
import timber.log.Timber
import java.io.Serializable

data class AuthorizationItemViewModel(
    val authorizationID: ID,
    val authorizationCode: String,
    val title: String,
    val description: DescriptionData,
    val validSeconds: Int,
    val endTime: DateTime,
    val startTime: DateTime,
    val connectionID: ID,
    val connectionName: String,
    val connectionLogoUrl: String?,
    val apiVersion: String,
    var status: AuthorizationStatus = AuthorizationStatus.PENDING
) : Serializable {
    private val secondsOfLifeOfFinalModel = 4
    var destroyAt: DateTime? = null

    /**
     * Set new viewMode and if is final mode set destroyAt
     */
    fun setNewStatus(newStatus: AuthorizationStatus) {
        this.status = newStatus
        this.destroyAt = if (newStatus.isFinalStatus()) DateTime.now().plusSeconds(secondsOfLifeOfFinalModel) else null
    }

    /**
     * Check that should be set viewMode == TIME_OUT
     *
     * @return Boolean, true if model is expired and does not have final viewMode
     */
    val shouldBeSetTimeOutMode: Boolean
        get() = this.isExpired && !this.hasFinalStatus && this.status != AuthorizationStatus.LOADING

    /**
     * Check that model can be authorized
     *
     * @return Boolean, true viewMode is default mode
     */
    val canBeAuthorized: Boolean
        get() = status === AuthorizationStatus.PENDING

    /**
     * Check that model has final viewMode
     *
     * @return Boolean, true viewMode is final mode
     */
    val hasFinalStatus: Boolean
        get() = status.isFinalStatus()

    /**
     * Check that `time views` should be hidden
     *
     * @return View.INVISIBLE if view mode is UNAVAILABLE or LOADING or View.VISIBLE
     */
    val timeViewVisibility: Int
        get() {
            val invisible = status == AuthorizationStatus.UNAVAILABLE || status == AuthorizationStatus.LOADING
            return if (invisible) View.INVISIBLE else View.VISIBLE
        }

    /**
     * Check that `time views` should ignore time changes.
     * freeze last time state
     *
     * @return Boolean, true if view mode is not default
     */
    val ignoreTimeUpdate: Boolean
        get() = status !== AuthorizationStatus.PENDING

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

    val isV2Api: Boolean
        get() = apiVersion == API_V2_VERSION
}

/**
 * Converts AuthorizationData in AuthorizationViewModel (API v1)
 *
 * @receiver Authorization
 * @param connection - Connection
 * @return AuthorizationViewModel
 */
fun AuthorizationData.toAuthorizationItemViewModel(connection: ConnectionAbs): AuthorizationItemViewModel? {
    return try {
        AuthorizationItemViewModel(
            title = this.title,
            description = this.description.toDescriptionData(),
            connectionID = connection.id,
            connectionName = connection.name,
            connectionLogoUrl = connection.logoUrl,
            validSeconds = authorizationExpirationPeriod(this.createdAt, this.expiresAt),
            endTime = this.expiresAt,
            startTime = this.createdAt ?: DateTime(0L),
            authorizationID = this.id,
            authorizationCode = this.authorizationCode ?: "",
            apiVersion = API_V1_VERSION,
            status = AuthorizationStatus.PENDING
        )
    } catch (e: Exception) {
        Timber.e(
            e,
            "Something went wrong %s %s",
            "Provider name: ${connection.name}",
            "id: ${connection.id}"
        )
        null
    }
}

/**
 * Converts AuthorizationData in AuthorizationViewModel (API v1)
 *
 * @receiver Authorization
 * @param connection - Connection
 * @return AuthorizationViewModel
 */
fun AuthorizationV2Data.toAuthorizationItemViewModel(connection: ConnectionAbs): AuthorizationItemViewModel? {
    return try {
        AuthorizationItemViewModel(
            title = this.title,
            description = this.description,
            connectionID = connection.id,
            connectionName = connection.name,
            connectionLogoUrl = connection.logoUrl,
            validSeconds = authorizationExpirationPeriod(this.createdAt, this.expiresAt),
            endTime = this.expiresAt,
            startTime = this.createdAt ?: DateTime(0L),
            authorizationID = this.authorizationID ?: "",
            authorizationCode = this.authorizationCode ?: "",
            apiVersion = API_V2_VERSION,
            status = this.status?.toAuthorizationStatus() ?: AuthorizationStatus.PENDING
        )
    } catch (e: Exception) {
        Timber.e(
            e,
            "Something went wrong %s %s",
            "Provider name: ${connection.name}",
            "id: ${connection.id}"
        )
        null
    }
}

/**
 * Merging of collections of AuthorizationItemViewModel
 *
 * @receiver oldViewModels previously stored list of AuthorizationViewModel's
 * @param newViewModels newly received list of AuthorizationViewModel's
 *
 * @return List, result of merging
 */
fun List<AuthorizationItemViewModel>.merge(
    newViewModels: List<AuthorizationItemViewModel>,
    newModelsApiVersion: String
): List<AuthorizationItemViewModel> {
    val filteredOldModels = this.filter {
        it.hasFinalStatus || it.apiVersion != newModelsApiVersion
    }
    val filteredNewModels = newViewModels.filter {
        filteredOldModels.noIdentifier(authorizationID = it.authorizationID, connectionID = it.connectionID)
    }
    return (filteredNewModels + filteredOldModels).sortedBy { it.startTime }
}

private fun List<AuthorizationItemViewModel>.noIdentifier(authorizationID: ID, connectionID: ID): Boolean {
    return !this.any { it.hasIdentifier(authorizationID = authorizationID, connectionID = connectionID) }
}

private fun AuthorizationItemViewModel.hasIdentifier(authorizationID: ID, connectionID: ID): Boolean {
    return this.authorizationID == authorizationID && this.connectionID == connectionID
}

private fun authorizationExpirationPeriod(startDate: DateTime?, endDate: DateTime): Int {
    return secondsBetweenDates(startDate ?: return 0, endDate)
}

private fun String.toDescriptionData(): DescriptionData {
    return if (this.hasHTMLTags()) DescriptionData(html = DescriptionHTMLData(html = this))
    else DescriptionData(text = DescriptionTextData(text = this))
}
