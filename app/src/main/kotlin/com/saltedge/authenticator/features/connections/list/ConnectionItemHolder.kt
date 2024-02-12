/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.connections.list

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.connections.common.ConnectionItem
import com.saltedge.authenticator.features.consents.common.consentsCountPrefixForConnection
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.tools.appendColoredText
import com.saltedge.authenticator.tools.inflateListItemView
import com.saltedge.authenticator.tools.loadImage
import com.saltedge.authenticator.tools.mediumTypefaceSpan

class ConnectionItemHolder(parent: ViewGroup, private val listener: ListItemClickListener?) :
    RecyclerView.ViewHolder(parent.inflateListItemView(R.layout.view_item_connection)) {

    private val logoImageView = itemView.findViewById<ShapeableImageView>(R.id.logoImageView)
    private val titleView = itemView.findViewById<TextView>(R.id.titleView)
    private val subTitleView = itemView.findViewById<TextView>(R.id.subTitleView)
    private val listItemView = itemView.findViewById<RelativeLayout>(R.id.listItemView)
    private val bgColor = ContextCompat.getColor(listItemView.context, R.color.white_and_blue_black)

    init {
        itemView.setOnClickListener {
            if (adapterPosition > RecyclerView.NO_POSITION)
                listener?.onListItemClick(itemIndex = adapterPosition)
        }
    }

    fun bind(item: ConnectionItem) {
        logoImageView.loadImage(
            imageUrl = item.logoUrl,
            placeholderId = R.drawable.shape_bg_app_logo
        )
        if (item.isChecked) listItemView.setBackgroundResource(R.drawable.stroke_background)
        else listItemView.setBackgroundColor(bgColor)

        titleView.text = item.name
        val consentsDescription = consentsCountPrefixForConnection(item.consentsCount, subTitleView.context)
        subTitleView.text = SpannableStringBuilder()
            .append(consentsDescription, subTitleView.context.mediumTypefaceSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            .appendColoredText(item.statusDescription, item.statusDescriptionColorRes, subTitleView.context)
    }
}
