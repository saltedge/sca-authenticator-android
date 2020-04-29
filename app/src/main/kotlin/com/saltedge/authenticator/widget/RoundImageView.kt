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
package com.saltedge.authenticator.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.saltedge.authenticator.R

class RoundedImageView : AppCompatImageView {

    private var circleBackgroundColor = ContextCompat.getColor(context, R.color.gray_extra_light)
    private val opacity: Int = 255
    private val bgPaint = createPaint()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onDraw(canvas: Canvas) {
        val diameter = Math.min(width, height)
        if (drawable == null || diameter == 0) return
        val originalBitmap = (drawable as? BitmapDrawable)?.bitmap ?: return
        val roundBitmap = originalBitmap.toCircleBitmap(diameter)
        canvas.drawBitmap(
            roundBitmap,
            (width - roundBitmap.width) / 2f,
            (height - roundBitmap.height) / 2f,
            null
        )
    }

    private fun Bitmap.toCircleBitmap(diameter: Int): Bitmap =
        emptyCircle(diameter).also {
            bgPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            val left = (it.width - this.width) / 2
            val top = (it.height - this.height) / 2
            Canvas(it).drawBitmap(this, left.toFloat(), top.toFloat(), bgPaint)
            bgPaint.xfermode = null
        }

    private fun emptyCircle(diameter: Int): Bitmap =
        Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888).also {
            Canvas(it).drawCircle(diameter / 2f, diameter / 2f, diameter / 2f, bgPaint)
        }

    private fun createPaint(): Paint {
        return Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
            alpha = opacity
            color = circleBackgroundColor
        }
    }
}

