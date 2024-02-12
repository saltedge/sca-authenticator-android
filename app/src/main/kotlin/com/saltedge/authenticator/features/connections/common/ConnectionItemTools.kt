/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.connections.common

import android.content.Context
import com.saltedge.authenticator.R
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.getStatus
import com.saltedge.authenticator.core.model.isActive
import com.saltedge.authenticator.core.tools.toDateTime
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.toDateFormatString

fun ConnectionAbs.convertConnectionToViewItem(
    context: Context,
    deviceLocationManager: DeviceLocationManagerAbs
): ConnectionItem {
    val shouldRequestLocationPermission = shouldRequestLocationPermission(
        geolocationRequired = this.geolocationRequired,
        locationPermissionsAreGranted = deviceLocationManager.locationPermissionsGranted()
    )
    return ConnectionItem(
        guid = this.guid,
        connectionId = this.id,
        name = this.name,
        statusDescription = getConnectionStatusDescription(
            context = context,
            connection = this,
            shouldRequestPermission = shouldRequestLocationPermission
        ),
        statusDescriptionColorRes = getConnectionStatusColorRes(
            connection = this,
            shouldRequestPermission = shouldRequestLocationPermission
        ),
        logoUrl = this.logoUrl,
        isActive = this.isActive(),
        isChecked = false,
        apiVersion = this.apiVersion,
        email = this.supportEmail,
        shouldRequestLocationPermission = shouldRequestLocationPermission
    )
}

fun List<ConnectionAbs>.convertConnectionsToViewItems(
    context: Context,
    locationManager: DeviceLocationManagerAbs
): List<ConnectionItem> {
    return this.map { connection ->
        connection.convertConnectionToViewItem(context, locationManager)
    }
}

/**
 * Should request permission if geolocationRequired is mandatory and location permissions are not granted
 *
 * @return Boolean
 */
fun shouldRequestLocationPermission(
    geolocationRequired: Boolean?,
    locationPermissionsAreGranted: Boolean
): Boolean {
    return geolocationRequired == true && !locationPermissionsAreGranted
}

private fun getConnectionStatusDescription(
    connection: ConnectionAbs,
    shouldRequestPermission: Boolean,
    context: Context
): String {
    return when (connection.getStatus()) {
        ConnectionStatus.INACTIVE -> context.getString(R.string.connection_status_inactive)
        ConnectionStatus.ACTIVE -> {
            val date =connection.updatedAt.toDateTime().toDateFormatString(context)
            if (shouldRequestPermission) {
                context.getString(R.string.connection_status_access_location)
            }
            else "${context.getString(R.string.connection_status_linked_on)} $date"
        }
    }
}

private fun getConnectionStatusColorRes(connection: ConnectionAbs, shouldRequestPermission: Boolean): ResId {
    return when (connection.getStatus()) {
        ConnectionStatus.INACTIVE -> R.color.red_and_red_light
        ConnectionStatus.ACTIVE -> {
            if (shouldRequestPermission) R.color.yellow
            else R.color.dark_60_and_grey_100
        }
    }
}
