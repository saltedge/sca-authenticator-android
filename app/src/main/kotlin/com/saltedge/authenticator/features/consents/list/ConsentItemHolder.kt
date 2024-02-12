/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.consents.list

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.tools.inflateListItemView

class ConsentItemHolder(parent: ViewGroup, private val listener: ListItemClickListener?) :
    RecyclerView.ViewHolder(parent.inflateListItemView(R.layout.view_item_consent))
{
    private val consentTitleLabel = itemView.findViewById<TextView>(R.id.consentTitleLabel)
    private val consentDescriptionLabel = itemView.findViewById<TextView>(R.id.consentDescriptionLabel)
    private val expiresLabel = itemView.findViewById<TextView>(R.id.expiresLabel)

    init {
        itemView.setOnClickListener {
            if (adapterPosition > RecyclerView.NO_POSITION) {
                listener?.onListItemClick(itemIndex = adapterPosition)
            }
        }
    }

    fun bind(item: ConsentItem) {
        consentTitleLabel.text = item.tppName
        consentDescriptionLabel.text = item.consentTypeDescription
        expiresLabel.text = item.expiresAtDescription
    }
}
