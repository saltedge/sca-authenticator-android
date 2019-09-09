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
import com.saltedge.authenticator.features.authorizations.common.AuthorizationViewModel
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.tool.parseHTML

class AuthorizationsContentPagerAdapter(
    context: Context
) : AuthorizationsPagerAdapter(), View.OnClickListener {

    var listener: ListItemClickListener? = null
    private val layoutInflater: LayoutInflater = //move in adapter
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var itemPosition: Int = 0

    override fun instantiateItem(container: ViewGroup, position: Int): Any { //move in adapter
        return inflatePageView(position).apply { container.addView(this, 0) }
    }

    override fun onClick(view: View?) {
        listener?.onListItemClick(
            itemIndex = getItemPosition(data),
            itemCode = (data[itemPosition]).authorizationId,
            itemViewId = view?.id ?: return
        )
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        itemPosition = position
    }

    private fun inflatePageView(position: Int): View {
        val pageView = layoutInflater.inflate(R.layout.authorization_item, null)
        return pageView.apply { updateViewContent(this, data[position]) }
    }

    private fun updateViewContent(pageView: View, model: AuthorizationViewModel) {
        pageView.findViewById<TextView>(R.id.titleTextView).text = model.title
        pageView.findViewById<TextView>(R.id.descriptionTextView).text = model.description.parseHTML()
        pageView.findViewById<Button>(R.id.negativeActionView).setOnClickListener(this)
        pageView.findViewById<Button>(R.id.positiveActionView).setOnClickListener(this)
    }
}
