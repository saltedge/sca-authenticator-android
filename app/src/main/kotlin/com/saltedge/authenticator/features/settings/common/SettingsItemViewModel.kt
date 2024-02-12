/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.settings.common

import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tools.ResId

data class SettingsItemViewModel(
    @StringRes val titleId: ResId,
    val titleColorRes: Int = R.color.dark_100_and_grey_40,
    @DrawableRes val iconId: ResId? = null,
    val description: String = "",
    var switchIsChecked: Boolean? = null,
    val itemIsClickable: Boolean = false
) {
    val iconResource: ResId = iconId ?: R.drawable.ic_menu_action_list
    val iconVisibility: Int = if (iconId == null) View.GONE else View.VISIBLE
    val descriptionVisibility: Int = if (description.isEmpty()) View.GONE else View.VISIBLE
    val switchVisibility: Int = if (switchIsChecked == null) View.GONE else View.VISIBLE
}
