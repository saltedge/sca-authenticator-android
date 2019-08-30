package com.saltedge.authenticator.features.authorizations.list

import android.view.View
import androidx.viewpager.widget.ViewPager

class CarouselPagerTransformer(private val pager: View) : ViewPager.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        val scale: Float
        val freeSpace = (pager.width - page.width) / 2f
        val adjustedPosition = position - freeSpace / page.width

        scale = if (adjustedPosition >= -1 && adjustedPosition <= 1) {
            val k = 1 - Math.abs(adjustedPosition)
            MIN_SCALE + (MAX_SCALE - MIN_SCALE) * k
        } else {
            MIN_SCALE
        }
        page.scaleX = scale
        page.scaleY = scale
    }

    companion object {
        private const val MAX_SCALE = 1f
        private const val MIN_SCALE = 0.85f
    }
}
