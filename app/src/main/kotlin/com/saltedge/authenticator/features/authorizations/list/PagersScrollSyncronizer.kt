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
package com.saltedge.authenticator.features.authorizations.list

import androidx.viewpager.widget.ViewPager

class PagersScrollSyncronizer {

    var firstViewPager: ViewPager? = null
    var secondViewPager: ViewPager? = null

    fun initViews(firstViewPager: ViewPager?, secondViewPager: ViewPager?) {
        this.firstViewPager = firstViewPager
        this.secondViewPager = secondViewPager
        firstViewPager?.addOnPageChangeListener(firstPageChangeListener)
        secondViewPager?.addOnPageChangeListener(secondPageChangeListener)
    }

    private val firstPageChangeListener = object : ViewPager.OnPageChangeListener {
        private var scrollState = ViewPager.SCROLL_STATE_IDLE

        override fun onPageScrollStateChanged(state: Int) {
            scrollState = state
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                secondViewPager?.setCurrentItem(firstViewPager?.currentItem ?: return, false)
            }
        }

        override fun onPageScrolled(position: Int, offset: Float, offsetPixels: Int) {
            if (scrollState == ViewPager.SCROLL_STATE_IDLE || offsetPixels == 0) return
            firstViewPager?.let { firstViewPager ->
                secondViewPager?.scrollTo((firstViewPager.scrollX.toFloat() * secondRelativeVelocity()).toInt(), 0)
            }
        }

        override fun onPageSelected(position: Int) {}
    }

    private val secondPageChangeListener = object : ViewPager.OnPageChangeListener {
        private var scrollState = ViewPager.SCROLL_STATE_IDLE

        override fun onPageScrollStateChanged(state: Int) {
            scrollState = state
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                firstViewPager?.setCurrentItem(secondViewPager?.currentItem ?: return, false)
            }
        }

        override fun onPageScrolled(position: Int, offset: Float, offsetPixels: Int) {
            if (scrollState == ViewPager.SCROLL_STATE_IDLE || offsetPixels == 0) return
            secondViewPager?.let { secondViewPager ->
                firstViewPager?.scrollTo((secondViewPager.scrollX.toFloat() * firstRelativeVelocity()).toInt(), 0)
            }
        }

        override fun onPageSelected(position: Int) {}
    }
    private val firstPageWidth: Int
        get() = firstViewPager?.let { it.width - it.paddingStart - it.paddingEnd } ?: 0
    private val secondPageWidth: Int
        get() = secondViewPager?.let { it.width - it.paddingStart - it.paddingEnd } ?: 0
    private fun secondRelativeVelocity(): Float {
        return if (firstPageWidth == 0) 1f else secondPageWidth.toFloat() / firstPageWidth.toFloat()
    }
    private fun firstRelativeVelocity(): Float {
        return if (secondPageWidth == 0) 1f else firstPageWidth.toFloat() / secondPageWidth.toFloat()
    }
}
