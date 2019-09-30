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
package com.saltedge.authenticator.features.settings.common

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.interfaces.CheckableListItemClickListener
import com.saltedge.authenticator.widget.list.AbstractListAdapter
import java.io.InvalidClassException

class SettingsAdapter(
    private val clickListener: CheckableListItemClickListener?
) : AbstractListAdapter() {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SettingsItemViewModel -> ItemViewType.SETTINGS_TITLE_VALUE
            is HeaderViewModel -> ItemViewType.HEADER
            else -> throw InvalidClassException("class ${getItem(position)?.javaClass?.name} is not handled")
        }.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (ItemViewType.values()[viewType]) {
            ItemViewType.SETTINGS_TITLE_VALUE -> SettingsItemViewHolder(parent, clickListener)
            ItemViewType.HEADER -> HeaderViewHolder(parent)
        }
    }

    override fun onBindHolder(holder: RecyclerView.ViewHolder, position: Int, item: Any) {
        when (holder) {
            is SettingsItemViewHolder -> holder.bind(item as SettingsItemViewModel)
        }
    }
}

enum class ItemViewType {
    SETTINGS_TITLE_VALUE,
    HEADER
}
