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
package com.saltedge.authenticator.widget.list

import androidx.recyclerview.widget.RecyclerView

abstract class AbstractListAdapter(initialData: List<Any> = emptyList()) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var _data: MutableList<Any> = initialData.toMutableList()
    var data: List<Any>
        get() = _data
        set(value) {
            _data = value.toMutableList()
            notifyDataSetChanged()
        }
    val isEmpty: Boolean
        get() = _data.isEmpty()

    fun updateItem(item: Any, itemId: Int) {
        val lastIndex = _data.lastIndex
        if (itemId in 0..lastIndex) {
            _data[itemId] = item
            notifyItemChanged(itemId)
        } else {
            _data.add(item)
            notifyItemChanged(lastIndex + 1)
        }
    }

    fun getItem(position: Int) = _data.getOrNull(position)

    override fun getItemCount(): Int = _data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindHolder(holder, position, _data[position])
    }

    abstract fun onBindHolder(holder: RecyclerView.ViewHolder, position: Int, item: Any)
}
