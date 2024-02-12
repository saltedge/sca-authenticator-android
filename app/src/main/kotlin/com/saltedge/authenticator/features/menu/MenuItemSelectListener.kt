/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.menu

interface MenuItemSelectListener {
    fun onMenuItemSelected(menuId: String = "", selectedItemId: Int)
}
