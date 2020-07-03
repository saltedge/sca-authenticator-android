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
package com.saltedge.authenticator.features.consents.list

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.tools.inflateListItemView

class ConsentItemHolder(parent: ViewGroup, private val listener: ListItemClickListener?) :
    RecyclerView.ViewHolder(parent.inflateListItemView(R.layout.view_item_consent)) {

    private val titleView = itemView.findViewById<TextView>(R.id.titleView)
    private val subTitleView = itemView.findViewById<TextView>(R.id.subTitleView)
    private val listItemView = itemView.findViewById<RelativeLayout>(R.id.listItemView)
    private val dateView = itemView.findViewById<TextView>(R.id.dateView)
    private val expiresInPrefix: Spanned
    private val baseColorSpan = ForegroundColorSpan(getColor(listItemView.context, R.color.dark_60_and_grey_100))
    private val alertSpan = ForegroundColorSpan(getColor(listItemView.context, R.color.red_and_red_light))

    init {
        itemView.setOnClickListener {
            if (adapterPosition > RecyclerView.NO_POSITION)
                listener?.onListItemClick(itemIndex = adapterPosition)
        }
        val expiresInString = listItemView.context.getString(R.string.expires_in)
        expiresInPrefix = SpannableStringBuilder(expiresInString).apply {
            setSpan(baseColorSpan, 0, this.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun bind(item: ConsentItemViewModel) {
        titleView.text = item.tppName
        subTitleView.text = item.consentTypeDescription

        val expiresAtSuffix = SpannableStringBuilder(item.expiresAt)
        expiresAtSuffix.setSpan(
            ForegroundColorSpan(getColor(listItemView.context, item.expiresAtColorRes)),
            0,
            expiresAtSuffix.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        dateView.text = TextUtils.concat(expiresInPrefix, " ", expiresAtSuffix)
    }
}
