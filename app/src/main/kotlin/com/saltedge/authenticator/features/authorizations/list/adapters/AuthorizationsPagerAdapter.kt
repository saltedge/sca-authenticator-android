package com.saltedge.authenticator.features.authorizations.list.adapters

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.saltedge.authenticator.features.authorizations.common.AuthorizationViewModel

abstract class AuthorizationsPagerAdapter(initialData: List<AuthorizationViewModel> = emptyList()) : PagerAdapter() {

    private var _data: MutableList<AuthorizationViewModel> = initialData.toMutableList()
    var data: List<AuthorizationViewModel>
        get() = _data
        set(value) {
            _data = value.toMutableList()
            notifyDataSetChanged()
        }
    val isEmpty: Boolean
        get() = _data.isEmpty()

    fun updateItem(item: AuthorizationViewModel, itemId: Int) {
        val lastIndex = _data.lastIndex
        if (itemId in 0..lastIndex) {
            _data[itemId] = item
            notifyDataSetChanged()
        }
    }

    override fun getCount(): Int = _data.size

    override fun getItemPosition(item: Any) = POSITION_NONE

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) =
        container.removeView(view as View)

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
}
