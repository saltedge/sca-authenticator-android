/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.menu

class BottomMenuPresenter {

    var menuId: String? = null
        private set
    var listItems: List<MenuItemData> = emptyList()
        private set

    fun setInitialData(
        menuId: String?,
        menuItems: List<MenuItemData>?
    ) {
        this.menuId = menuId
        menuItems?.let { listItems = menuItems }
    }
}
