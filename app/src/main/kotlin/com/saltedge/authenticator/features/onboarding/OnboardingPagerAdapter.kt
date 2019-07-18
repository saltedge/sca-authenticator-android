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
package com.saltedge.authenticator.features.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.saltedge.authenticator.R

class OnboardingPagerAdapter(val context: Context, private var items: List<OnboardingPageViewModel>) : PagerAdapter() {

    private val layoutInflater: LayoutInflater
        get() = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = inflatePageView(items[position])
        container.addView(view, 0)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }

    override fun isViewFromObject(view: View, item: Any) = (view == item)

    override fun getCount(): Int = items.size

    private fun inflatePageView(item: OnboardingPageViewModel): View {
        val view = layoutInflater.inflate(R.layout.page_onboarding, null)
        view.findViewById<TextView?>(R.id.titleView)?.setText(item.titleResId)
        view.findViewById<TextView?>(R.id.subTitleView)?.setText(item.subTitleResId)
        view.findViewById<ImageView?>(R.id.imageView)?.setImageResource(item.imageResId)
        return view
    }
}
