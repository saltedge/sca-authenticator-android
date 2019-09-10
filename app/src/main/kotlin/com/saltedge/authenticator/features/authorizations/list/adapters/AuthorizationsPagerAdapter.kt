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
import androidx.viewpager.widget.PagerAdapter
import com.saltedge.authenticator.features.authorizations.common.AuthorizationViewModel

abstract class AuthorizationsPagerAdapter(
    context: Context,
    private val layoutId: Int,
    initialData: List<AuthorizationViewModel> = emptyList()
) : PagerAdapter() {

    private var _data: MutableList<AuthorizationViewModel> = initialData.toMutableList()
    var data: List<AuthorizationViewModel>
        get() = _data
        set(value) {
            _data = value.toMutableList()
            notifyDataSetChanged()
        }
    val isEmpty: Boolean
        get() = _data.isEmpty()
    protected var itemPosition: Int = 0

    fun updateItem(item: AuthorizationViewModel, itemId: Int) {
        val lastIndex = _data.lastIndex
        if (itemId in 0..lastIndex) {
            _data[itemId] = item
            notifyDataSetChanged()
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any { //move in adapter
        return inflatePageView(position).apply { container.addView(this, 0) }
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

    abstract fun updateViewContent(pageView: View, model: AuthorizationViewModel)

    private fun inflatePageView(position: Int): View {
        val pageView = layoutInflater.inflate(layoutId, null)
        return pageView.apply { updateViewContent(this, data[position]) }
    }

    private val layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
}
