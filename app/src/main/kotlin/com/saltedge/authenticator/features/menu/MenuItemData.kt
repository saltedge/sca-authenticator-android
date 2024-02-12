/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.menu

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.saltedge.authenticator.tools.ResId
import java.io.Serializable

data class MenuItemData(
    val id: Int,
    @DrawableRes val iconRes: ResId,
    @StringRes val textRes: ResId? = null,
    val text: String? = null,
    val isActive: Boolean = true
) : Serializable
