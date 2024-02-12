/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.menu

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.tools.inflateListItemView

class MenuItemHolder(
    parent: ViewGroup,
    private val listener: ListItemClickListener?
) : RecyclerView.ViewHolder(parent.inflateListItemView(R.layout.view_item_menu_dialog)),
    View.OnClickListener {

    private val optionImageView = itemView.findViewById<ImageView>(R.id.menuItemImageView)
    private val optionTitleView = itemView.findViewById<TextView>(R.id.menuItemTitleView)
    private var itemId: Int = -1

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (adapterPosition > RecyclerView.NO_POSITION) {
            listener?.onListItemClick(itemIndex = adapterPosition, itemViewId = itemId)
        }
    }

    fun bind(item: MenuItemData) {
        optionImageView.setImageResource(item.iconRes)
        item.textRes?.let { optionTitleView.setText(it) }
        item.text?.let { optionTitleView.text = it }
        itemId = item.id
    }
}
