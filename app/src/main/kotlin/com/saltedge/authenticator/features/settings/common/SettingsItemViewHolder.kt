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

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.databinding.ViewItemSettingBinding
import com.saltedge.authenticator.interfaces.ListItemClickListener

class SettingsItemViewHolder(val binding: ViewItemSettingBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SettingsItemViewModel, listener: ListItemClickListener) {
        binding.root.setOnClickListener {
            listener.onListItemClick(item.titleId)
        }
        binding.titleView.setText(item.titleId)
        binding.imageView.setImageResource(item.iconResource)
        binding.imageView.visibility = item.iconVisibility
        binding.titleView.setTextColor(
            ContextCompat.getColor(
                binding.root.context,
                item.titleColorRes
            )
        )
        binding.valueView.visibility = item.descriptionVisibility
        binding.valueView.setText(item.description)
        binding.checkView.visibility = item.switchVisibility
        binding.checkView.isChecked = item.switchIsChecked ?: false
        binding.checkView.setOnCheckedChangeListener { buttonView, isChecked ->
            listener.onListItemCheckedStateChanged(item.titleId, isChecked)
        }
    }
}
