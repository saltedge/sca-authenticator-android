/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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

import android.graphics.*
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.charset.Charset
import java.security.MessageDigest
import kotlin.math.min

class RoundedBitmapTransformation(
    private var backgroundColor: Int,
    private var cornerRadius: Float
) : BitmapTransformation() {
    private val id = "com.saltedge.authenticator.widget.RoundedBitmapTransformation_v1_$cornerRadius"
    private val bgPaint = createPaint()
    private val opacity: Int = 255

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(id.toByteArray(Charset.forName("UTF-8")));
    }

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val diameter = min(outWidth, outHeight)
        return if (cornerRadius < 0 || cornerRadius > diameter / 2) {
            toTransform.toInsetBitmap(shape = createCircleShape(diameter))
        } else {
            toTransform.toInsetBitmap(shape = createRoundRectShape(outWidth, outHeight, cornerRadius))
        }
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is RoundedBitmapTransformation
    }

    override fun hashCode(): Int = id.hashCode()

    private fun Bitmap.toInsetBitmap(shape: Bitmap): Bitmap {
        bgPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val left = (shape.width - this.width) / 2
        val top = (shape.height - this.height) / 2
        Canvas(shape).drawBitmap(this, left.toFloat(), top.toFloat(), bgPaint)
        bgPaint.xfermode = null
        return shape
    }

    private fun createCircleShape(diameter: Int): Bitmap =
        Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888).also {
            Canvas(it).drawCircle(diameter / 2f, diameter / 2f, diameter / 2f, bgPaint)
        }

    private fun createRoundRectShape(rectWidth: Int, rectHeight: Int, radius: Float): Bitmap {
        val rectF = RectF(Rect(0, 0, rectWidth, rectHeight));
        val output = Bitmap.createBitmap(rectWidth, rectHeight, Bitmap.Config.ARGB_8888)
        Canvas(output).drawRoundRect(rectF, radius, radius, bgPaint)
        return output
    }

    private fun createPaint(): Paint {
        return Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
            alpha = opacity
            color = backgroundColor
        }
    }
}
