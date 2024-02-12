/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.authorizations.list.pagers

import android.content.Context
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import com.saltedge.authenticator.features.authorizations.common.AuthorizationContentView
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.tools.getOrPut

class AuthorizationsContentPagerAdapter(val context: Context) : AuthorizationsPagerAdapter(),
    View.OnClickListener {

    var listItemClickListener: ListItemClickListener? = null
    private val map = SparseArray<AuthorizationContentView>()

    override fun onClick(view: View?) {
        listItemClickListener?.onListItemClick(
            itemIndex = itemPosition,
            itemCode = (data[itemPosition]).authorizationID,
            itemViewId = view?.id ?: return
        )
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = map.getOrPut(position) {
            AuthorizationContentView(context = context)
        }
        view.setActionClickListener(this)
        updateViewContent(view, data[position])
        return view.apply { container.addView(this, 0) }
    }

    private fun updateViewContent(pageView: View, model: AuthorizationItemViewModel) {
        (pageView as AuthorizationContentView).also {
            it.setTitleAndDescription(model.title, model.description)
            it.setViewMode(model.status)
        }
    }
}
