/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.connections.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.features.connections.common.ConnectionItem
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.widget.list.AbstractListAdapter

class ConnectionsListAdapter(val clickListener: ListItemClickListener?) : AbstractListAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ConnectionItemHolder(parent, clickListener)

    override fun onBindHolder(holder: RecyclerView.ViewHolder, position: Int, item: Any) {
        (holder as ConnectionItemHolder).bind(item as ConnectionItem)
    }

    fun updateListItem(viewModel: ConnectionItem) {
        val itemIndex = data.indexOfFirst { (it as ConnectionItem).guid == viewModel.guid }
        if (itemIndex > -1) updateItem(viewModel, itemIndex)
    }
}
