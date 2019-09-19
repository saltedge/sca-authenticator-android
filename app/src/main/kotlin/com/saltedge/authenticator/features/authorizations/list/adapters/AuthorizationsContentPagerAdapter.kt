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
import android.view.View
import android.view.ViewGroup
import com.saltedge.authenticator.features.authorizations.common.AuthorizationContentView
import com.saltedge.authenticator.features.authorizations.common.AuthorizationViewModel
import com.saltedge.authenticator.interfaces.ListItemClickListener

class AuthorizationsContentPagerAdapter(val context: Context) :
    AuthorizationsPagerAdapter(),
    View.OnClickListener
{

    var listItemClickListener: ListItemClickListener? = null

    override fun onClick(view: View?) {
        listItemClickListener?.onListItemClick(
            itemIndex = itemPosition,
            itemCode = (data[itemPosition]).authorizationID,
            itemViewId = view?.id ?: return
        )
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = AuthorizationContentView(context = context)
        updateViewContent(view, data[position])
        return view.apply { container.addView(this, 0) }
    }

    private fun updateViewContent(pageView: View, model: AuthorizationViewModel) {
        (pageView as AuthorizationContentView).also {
            it.setTitle(model.title)
            it.setDescription(model.description)
            it.setActionClickListener(this)
        }
    }
}
