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

import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.interfaces.CheckableListItemClickListener
import com.saltedge.authenticator.tool.inflateListItemView
import com.saltedge.authenticator.tool.setVisible

class SettingsItemViewHolder(
    parent: ViewGroup,
    var listener: CheckableListItemClickListener?
) : RecyclerView.ViewHolder(parent.inflateListItemView(R.layout.view_item_setting)),
    View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private val titleView = itemView.findViewById<TextView>(R.id.titleView)
    private val valueView = itemView.findViewById<TextView>(R.id.valueView)
    private var checkView = itemView.findViewById<Switch>(R.id.checkView)
    private var code = -1
    private var bottomFullDivider = itemView.findViewById<View>(R.id.bottomFullDivider)

    override fun onClick(v: View?) {
        if (adapterPosition > RecyclerView.NO_POSITION) listener?.onListItemClick(itemViewId = code)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (adapterPosition > RecyclerView.NO_POSITION)
            listener?.onListItemCheckedStateChanged(itemId = code, checked = isChecked)
    }

    fun bind(item: SettingsItemViewModel, bottomSeparator: Boolean) {
        code = item.titleId
        titleView.setText(item.titleId)
        titleView.setTextColor(ContextCompat.getColor(titleView.context, item.colorResId))
        valueView.setVisible(item.value != null)
        if (item.value != null) {
            valueView.text = item.value
        }
        checkView.setVisible(item.switchEnabled != null)
        if (item.switchEnabled != null) {
            checkView.isEnabled = item.switchEnabled
        }

        bottomFullDivider?.setVisible(bottomSeparator)

        checkView.setOnCheckedChangeListener(null)
        checkView.isChecked = item.isChecked
        checkView.setOnCheckedChangeListener(this)

        itemView.setOnClickListener(if (item.itemIsClickable) this else null)
    }
}
