/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.authorizations.list.pagers

import androidx.viewpager.widget.ViewPager

class PagersScrollSynchronizer {

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
