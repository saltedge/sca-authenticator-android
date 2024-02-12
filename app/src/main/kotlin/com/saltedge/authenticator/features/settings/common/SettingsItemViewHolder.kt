/*
 * Copyright (c) 2019 Salt Edge Inc.
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
