/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.connections.list.menu

import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.connections.common.ConnectionItem
import com.saltedge.authenticator.features.menu.MenuItemData

enum class ConnectionsListMenuItemType {
    RECONNECT, RENAME, SUPPORT, CONSENTS, DELETE, LOCATION, INFO
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
    menuItems.add(
        MenuItemData(
            id = ConnectionsListMenuItemType.INFO.ordinal,
            iconRes = R.drawable.ic_menu_id,
            text = "ID: ${item.connectionId}",
            isActive = false
        )
    )
    return menuItems
}
