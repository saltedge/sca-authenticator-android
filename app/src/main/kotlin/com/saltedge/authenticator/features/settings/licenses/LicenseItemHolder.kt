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
package com.saltedge.authenticator.features.settings.licenses

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.tool.inflateListItemView

class LicenseItemHolder(
    parent: ViewGroup,
    var listener: ListItemClickListener?
) : RecyclerView.ViewHolder(parent.inflateListItemView(R.layout.view_item_license)),
    View.OnClickListener {

    private val titleView = itemView.findViewById<TextView>(R.id.itemTitleView)

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (adapterPosition > RecyclerView.NO_POSITION) {
            listener?.onListItemClick(
                itemIndex = adapterPosition,
                itemCode = titleView.text as String
            )
        }
    }

    fun bind(item: String) {
        titleView.text = item
    }
}
