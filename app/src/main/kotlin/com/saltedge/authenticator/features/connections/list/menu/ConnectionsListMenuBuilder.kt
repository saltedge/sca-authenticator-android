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
package com.saltedge.authenticator.features.connections.list.menu

import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.connections.common.ConnectionItem
import com.saltedge.authenticator.features.menu.MenuItemData

enum class ConnectionsListMenuItemType {
    RECONNECT, RENAME, SUPPORT, CONSENTS, DELETE, LOCATION
}

fun buildConnectionsListMenu(item: ConnectionItem): List<MenuItemData> {
    val menuItems = mutableListOf<MenuItemData>()
    if (!item.isActive) {
        menuItems.add(
            MenuItemData(
                id = ConnectionsListMenuItemType.RECONNECT.ordinal,
                iconRes = R.drawable.ic_menu_reconnect_24dp,
                textRes = R.string.actions_reconnect
            )
        )
    }
    menuItems.addAll(
        listOf(
            MenuItemData(
                id = ConnectionsListMenuItemType.RENAME.ordinal,
                iconRes = R.drawable.ic_menu_edit_24dp,
                textRes = R.string.actions_rename
            ),
            MenuItemData(
                id = ConnectionsListMenuItemType.SUPPORT.ordinal,
                iconRes = R.drawable.ic_contact_support_24dp,
                textRes = R.string.actions_contact_support
            )
        )
    )
    if (item.consentsCount > 0) {
        menuItems.add(
            MenuItemData(
                id = ConnectionsListMenuItemType.CONSENTS.ordinal,
                iconRes = R.drawable.ic_view_consents_24dp,
                textRes = R.string.actions_view_consents
            )
        )
    }
    if (item.shouldRequestLocationPermission) {
        menuItems.add(
            MenuItemData(
                id = ConnectionsListMenuItemType.LOCATION.ordinal,
                iconRes = R.drawable.ic_view_location_24dp,
                textRes = R.string.actions_view_location
            )
        )
    }
    menuItems.add(
        MenuItemData(
            id = ConnectionsListMenuItemType.DELETE.ordinal,
            iconRes = if (item.isActive) R.drawable.ic_menu_delete_24dp else R.drawable.ic_menu_remove_24dp,
            textRes = if (item.isActive) R.string.actions_delete else R.string.actions_remove
        )
    )
    return menuItems
}
