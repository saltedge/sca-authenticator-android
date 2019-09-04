package com.saltedge.authenticator.features.authorizations.list.adapters

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.saltedge.authenticator.features.authorizations.common.AuthorizationViewModel

abstract class AuthorizationsPagerAdapter : PagerAdapter() {

    private var _data: List<AuthorizationViewModel> = emptyList()
    var data: List<AuthorizationViewModel>
        get() = _data
        set(value) {
            _data = value.toMutableList()
            notifyDataSetChanged()
        }
    val isEmpty: Boolean
        get() = _data.isEmpty()

    override fun getCount(): Int = data.size

    override fun getItemPosition(item: Any) = POSITION_NONE

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) =
        container.removeView(view as View)

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
}
