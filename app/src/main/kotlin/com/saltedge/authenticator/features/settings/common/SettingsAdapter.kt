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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.databinding.SettingsHeaderBinding
import com.saltedge.authenticator.databinding.SettingsItemBinding
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.widget.list.AbstractListAdapter

class SettingsAdapter(
    private val listener: ListItemClickListener
) : AbstractListAdapter() {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) is SettingsHeaderViewModelModel) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> {
                val binding: SettingsHeaderBinding = DataBindingUtil
                    .inflate(inflater, R.layout.view_item_setting_header, parent, false)
                SettingsHeaderViewHolder(binding)
            }
            else -> {
                val binding: SettingsItemBinding = DataBindingUtil
                    .inflate(inflater, R.layout.view_item_setting, parent, false)
                binding.listener = listener
                SettingsItemViewHolder(binding)
            }
        }
    }

    override fun onBindHolder(holder: RecyclerView.ViewHolder, position: Int, item: Any) {
        if (holder is SettingsHeaderViewHolder) holder.bind(item as SettingsHeaderViewModelModel)
        else (holder as SettingsItemViewHolder).bind(item as SettingsItemViewModel)
    }
}
