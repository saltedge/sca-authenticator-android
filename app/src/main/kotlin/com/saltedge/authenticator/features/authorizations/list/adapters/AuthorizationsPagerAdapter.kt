package com.saltedge.authenticator.features.authorizations.list.adapters

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

abstract class AuthorizationsPagerAdapter(initialData: List<Any> = emptyList()) : PagerAdapter() {

    private var _data: MutableList<Any> = initialData.toMutableList()
    var data: List<Any>
        get() = _data
        set(value) {
            _data = value.toMutableList()
            notifyDataSetChanged()
        }
    val isEmpty: Boolean
        get() = _data.isEmpty()

    fun updateItem(item: Any, itemId: Int) {
        val lastIndex = _data.lastIndex
        if (itemId in 0..lastIndex) {
            _data[itemId] = item
            notifyDataSetChanged()
        }
    }

    override fun getCount(): Int = data.size

    override fun getItemPosition(item: Any) = POSITION_NONE

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) =
        container.removeView(view as View)

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
}
