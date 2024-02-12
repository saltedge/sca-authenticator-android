/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.interfaces

import com.saltedge.authenticator.tools.ResId

interface ActivityComponentsContract {
    fun updateAppbar(
        titleResId: ResId? = null,
        title: String? = null,
        backActionImageResId: ResId? = null,
        showMenu: Array<MenuItem> = emptyArray()
    )
    fun onLanguageChanged()
}

enum class MenuItem {
    SCAN_QR, CUSTOM_NIGHT_MODE, MORE_MENU
}
