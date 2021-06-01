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
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.toDateFormatString

fun Connection.convertConnectionToViewModel(context: Context): ConnectionItem {
    return ConnectionItem(
        guid = this.guid,
        connectionId = this.id,
        name = this.name,
        statusDescription = getConnectionStatusDescription(context = context, connection = this),
        statusDescriptionColorRes = getConnectionStatusColor(connection = this),
        logoUrl = this.logoUrl,
        isActive = this.isActive(),
        isChecked = false,
        apiVersion = this.apiVersion,
        email = this.supportEmail
    )
}

fun List<Connection>.convertConnectionsToViewModels(context: Context): List<ConnectionItem> {
    return this.map { connection -> connection.convertConnectionToViewModel(context) }
}

private fun getConnectionStatusDescription(context: Context, connection: Connection): String {
    return when (connection.getStatus()) {
        ConnectionStatus.INACTIVE -> context.getString(R.string.connection_status_inactive)
        ConnectionStatus.ACTIVE -> {
            val date = connection.updatedAt.toDateTime().toDateFormatString(context)
            "${context.getString(R.string.connection_status_linked_on)} $date"
        }
    }
}

fun getConnectionStatusColor(connection: Connection): ResId {
    return when (connection.getStatus()) {
        ConnectionStatus.INACTIVE -> R.color.red_and_red_light
        ConnectionStatus.ACTIVE -> R.color.dark_60_and_grey_100
    }
}
