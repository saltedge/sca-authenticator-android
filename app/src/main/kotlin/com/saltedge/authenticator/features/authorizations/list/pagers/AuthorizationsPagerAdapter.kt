/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.authorizations.list.pagers

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel

abstract class AuthorizationsPagerAdapter : PagerAdapter() {

    private var _data: MutableList<AuthorizationItemViewModel> = mutableListOf()
    var data: List<AuthorizationItemViewModel>
        get() = _data
        set(value) {
            _data = value.toMutableList()
            notifyDataSetChanged()
        }
    val isEmpty: Boolean
        get() = _data.isEmpty()
    protected var itemPosition: Int = 0

    fun updateItem(item: AuthorizationItemViewModel, itemId: Int) {
        val lastIndex = _data.lastIndex
        if (itemId in 0..lastIndex) {
            _data[itemId] = item
            notifyDataSetChanged()
        }
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        itemPosition = position
    }

    override fun getCount(): Int = _data.size

    override fun getItemPosition(item: Any) = POSITION_NONE

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) =
        container.removeView(view as View)

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
}
