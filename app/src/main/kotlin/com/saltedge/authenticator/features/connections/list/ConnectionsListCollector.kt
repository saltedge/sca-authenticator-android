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
import com.saltedge.authenticator.features.connections.common.ConnectionItemViewModel
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.model.GUID
import com.saltedge.authenticator.sdk.model.connection.ConnectionAbs
import com.saltedge.authenticator.sdk.model.connection.ConnectionStatus
import com.saltedge.authenticator.sdk.model.connection.getStatus
import com.saltedge.authenticator.sdk.tools.toDateTime
import com.saltedge.authenticator.tools.toDateFormatString

fun collectAllConnectionsViewModels(
    repository: ConnectionsRepositoryAbs,
    context: Context
): List<ConnectionItemViewModel> {
    return repository.getAllConnections()
        .sortedBy { it.createdAt }
        .convertConnectionsToViewModels(context)
}

fun collectConnectionViewModel(
    guid: GUID,
    repository: ConnectionsRepositoryAbs,
    context: Context
): ConnectionItemViewModel? {
    return repository.getByGuid(guid)?.convertConnectionToViewModel(context)
}

fun Connection.convertConnectionToViewModel(context: Context): ConnectionItemViewModel {
    return ConnectionItemViewModel(
        guid = this.guid,
        connectionId = this.id,
        code = this.code,
        name = this.name,
        statusDescription = getConnectionStatusDescription(
            context = context,
            connection = this
        ),
        logoUrl = this.logoUrl,
        reconnectOptionIsVisible = isActiveConnection(this),
        deleteMenuItemText = getConnectionDeleteTextResId(this),
        deleteMenuItemImage = getConnectionDeleteImageResId(this),
        isChecked = false
    )
}

fun List<Connection>.convertConnectionsToViewModels(context: Context): List<ConnectionItemViewModel> {
    return this.map { connection -> connection.convertConnectionToViewModel(context) }
}

private fun isActiveConnection(connection: ConnectionAbs): Boolean {
    return connection.getStatus() !== ConnectionStatus.ACTIVE
}

private fun getConnectionDeleteTextResId(connection: ConnectionAbs): Int {
    return if (connection.getStatus() === ConnectionStatus.ACTIVE) R.string.actions_delete else R.string.actions_remove
}

private fun getConnectionDeleteImageResId(connection: ConnectionAbs): Int {
    return if (connection.getStatus() === ConnectionStatus.ACTIVE) R.drawable.ic_menu_delete_24dp else R.drawable.ic_menu_remove_24dp
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
