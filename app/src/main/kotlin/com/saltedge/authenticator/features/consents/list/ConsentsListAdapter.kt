/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.consents.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.widget.list.AbstractListAdapter

class ConsentsListAdapter(val clickListener: ListItemClickListener?) : AbstractListAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ConsentItemHolder(parent, clickListener)

    override fun onBindHolder(holder: RecyclerView.ViewHolder, position: Int, item: Any) {
        (holder as ConsentItemHolder).bind(item as ConsentItem)
    }
}
