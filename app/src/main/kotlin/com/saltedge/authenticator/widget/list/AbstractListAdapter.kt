/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.widget.list

import androidx.recyclerview.widget.RecyclerView

abstract class AbstractListAdapter(initialData: List<Any> = emptyList()) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
