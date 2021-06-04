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
package com.saltedge.authenticator.features.connections.list

import android.content.Context
import com.saltedge.authenticator.R
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.getStatus
import com.saltedge.authenticator.core.model.isActive
import com.saltedge.authenticator.core.tools.toDateTime
import com.saltedge.authenticator.features.connections.common.ConnectionItem
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.toDateFormatString

fun Connection.convertConnectionToViewModel(context: Context, deviceLocationManager: DeviceLocationManagerAbs): ConnectionItem {
    val locationPermissionsIsGranted: Boolean = deviceLocationManager.locationPermissionsGranted(context)
    val shouldGrantAccess = checkGrantAccessToLocationData(geolocationRequired = this.geolocationRequired, locationPermissionsIsGranted = locationPermissionsIsGranted)
    return ConnectionItem(
        guid = this.guid,
        connectionId = this.id,
        name = this.name,
        statusDescription = getConnectionStatusDescription(context = context, connection = this, grantAccessToLocationData = shouldGrantAccess),
        statusDescriptionColorRes = getConnectionStatusColor(connection = this, grantAccessToLocationData = shouldGrantAccess),
        logoUrl = this.logoUrl,
        isActive = this.isActive(),
        isChecked = false,
        apiVersion = this.apiVersion,
        email = this.supportEmail,
        locationPermissionRequired = shouldGrantAccess
    )
}

fun List<Connection>.convertConnectionsToViewModels(context: Context, locationManager: DeviceLocationManagerAbs): List<ConnectionItem> {
    return this.map { connection -> connection.convertConnectionToViewModel(context, locationManager) }
}

private fun getConnectionStatusDescription(
    context: Context,
    connection: Connection,
    grantAccessToLocationData: Boolean
): String {
    return when (connection.getStatus()) {
        ConnectionStatus.INACTIVE -> context.getString(R.string.connection_status_inactive)
        ConnectionStatus.ACTIVE -> {
            val date = connection.updatedAt.toDateTime().toDateFormatString(context)
            if (grantAccessToLocationData) {
                context.getString(R.string.connection_status_access_location)
            }
            else "${context.getString(R.string.connection_status_linked_on)} $date"
        }
    }
}

private fun getConnectionStatusColor(connection: Connection, grantAccessToLocationData: Boolean): ResId {
    return when (connection.getStatus()) {
        ConnectionStatus.INACTIVE -> R.color.red_and_red_light
        ConnectionStatus.ACTIVE -> {
            if (grantAccessToLocationData) R.color.yellow
            else R.color.dark_60_and_grey_100
        }
    }
}

/**
 * Check if location permissions are granted and geolocation is required
 */
private fun checkGrantAccessToLocationData(geolocationRequired: Boolean?, locationPermissionsIsGranted: Boolean): Boolean {
    return (geolocationRequired == null
        || geolocationRequired == false)
        || !locationPermissionsIsGranted
        || geolocationRequired == true && !locationPermissionsIsGranted
        || geolocationRequired == false && locationPermissionsIsGranted
}
