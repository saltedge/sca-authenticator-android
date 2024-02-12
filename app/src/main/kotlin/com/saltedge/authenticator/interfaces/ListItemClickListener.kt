/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.interfaces

interface ListItemClickListener {
    fun onListItemClick(itemIndex: Int = -1, itemCode: String = "", itemViewId: Int = -1) {}
    fun onListItemClick(itemId: Int) {}
    fun onListItemCheckedStateChanged(itemId: Int = -1, checked: Boolean) {}
}
