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
package com.saltedge.authenticator.features.authorizations.list.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.tool.parseHTML
import com.saltedge.authenticator.tool.setFont

class AuthorizationsContentPagerAdapter(
    context: Context
) : AuthorizationsPagerAdapter(), View.OnClickListener {

    var listener: ListItemClickListener? = null
    private val layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var itemPosition: Int = 0
]
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return inflatePageView(position).also { container.addView(it, 0) }
    }

    override fun onClick(view: View?) {
        notifyClickListener(viewId = view?.id ?: return, code = data[itemPosition].authorizationId)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        itemPosition = position
    }

    private fun inflatePageView(position: Int): View {
        val pageView = layoutInflater.inflate(R.layout.authorization_item, null)
        return pageView.apply { updateViewContent(this, position) }
    }

    private fun notifyClickListener(viewId: Int, code: String) {
        listener?.onListItemClick(
            itemIndex = getItemPosition(data),
            itemCode = code,
            itemViewId = viewId
        )
    }

    private fun updateViewContent(pageView: View, position: Int) {
        pageView.findViewById<TextView>(R.id.titleTextView).text = data[position].title
        pageView.findViewById<TextView>(R.id.descriptionTextView).text =
            data[position].description.parseHTML()
        pageView.findViewById<Button>(R.id.negativeActionView).setOnClickListener(this)
        pageView.findViewById<Button>(R.id.positiveActionView).setOnClickListener(this)

        pageView.findViewById<Button>(R.id.negativeActionView).setFont(R.font.roboto_medium)
        pageView.findViewById<Button>(R.id.positiveActionView).setFont(R.font.roboto_medium)
    }
}
