/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.menu

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.widget.list.AbstractListAdapter

class MenuItemsAdapter(private val clickListener: ListItemClickListener?) : AbstractListAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MenuItemHolder(parent, clickListener)

    override fun onBindHolder(holder: RecyclerView.ViewHolder, position: Int, item: Any) {
        (holder as MenuItemHolder).bind(item as MenuItemData)
    }
}
