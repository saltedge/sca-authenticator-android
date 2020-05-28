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
package com.saltedge.authenticator.features.settings.common

import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tools.ResId

data class SettingsItemModel(
    @StringRes val titleId: ResId,
    val titleColorRes: Int = R.color.primary_text,
    @DrawableRes val iconId: ResId? = null,
    val description: String = "",
    val switchIsChecked: Boolean? = null,
    val itemIsClickable: Boolean = false
) {
    val iconResource: ResId = iconId ?: R.drawable.ic_menu_action_list
    val iconVisibility: Int = if (iconId == null) View.GONE else View.VISIBLE
    val descriptionVisibility: Int = if (description.isEmpty()) View.GONE else View.VISIBLE
    val switchVisibility: Int = if (switchIsChecked == null) View.GONE else View.VISIBLE
}
