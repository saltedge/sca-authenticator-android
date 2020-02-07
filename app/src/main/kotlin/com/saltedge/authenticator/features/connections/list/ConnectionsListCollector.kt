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
import com.saltedge.authenticator.features.connections.common.ConnectionViewModel
import com.saltedge.authenticator.model.db.Connection
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.model.ConnectionAbs
import com.saltedge.authenticator.sdk.model.ConnectionStatus
import com.saltedge.authenticator.sdk.model.getStatus
import com.saltedge.authenticator.sdk.tools.toDateTime
import com.saltedge.authenticator.tool.toLongDateString

fun collectAllConnectionsViewModels(
    repository: ConnectionsRepositoryAbs,
    context: Context
): List<ConnectionViewModel> {
    return repository.getAllConnections()
        .sortedBy { it.createdAt }
        .convertConnectionsToViewModels(context)
}

fun List<Connection>.convertConnectionsToViewModels(context: Context): List<ConnectionViewModel> {
    return this.map { connection ->
        ConnectionViewModel(
            guid = connection.guid,
            code = connection.code,
            name = connection.name,
            statusDescription = getConnectionStatusDescription(
                context = context,
                connection = connection
            ),
            statusColorResId = getConnectionStateColorResId(connection),
            logoUrl = connection.logoUrl
        )
    }
}

private fun getConnectionStateColorResId(connection: ConnectionAbs): Int {
    return if (connection.getStatus() === ConnectionStatus.ACTIVE) R.color.gray_dark else R.color.red
}

private fun getConnectionStatusDescription(context: Context, connection: Connection): String {
    return when (connection.getStatus()) {
        ConnectionStatus.INACTIVE -> context.getString(R.string.connection_status_inactive)
        ConnectionStatus.ACTIVE -> {
            val date = connection.updatedAt.toDateTime().toLongDateString(context)
            "${context.getString(R.string.connection_status_connected_on)} $date"
        }
    }
}
