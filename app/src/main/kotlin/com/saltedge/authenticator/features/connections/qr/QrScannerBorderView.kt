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
package com.saltedge.authenticator.features.connections.qr

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.saltedge.authenticator.R

class QrScannerBorderView : View {

    private var borderRect = RectF()
    private val lineHeight: Float
        get() = context.resources.getDimension(R.dimen.dp_8)
    private val lineLength: Float
        get() = borderRect.width() / 3
    private var linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.gray_extra_light_50)
        strokeWidth = lineHeight
        strokeCap = Paint.Cap.ROUND
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val rectHalfSize = Math.min(w, h) * 0.8f / 2
        val centerX = w.toFloat() / 2
        val centerY = h.toFloat() / 2
        borderRect = RectF(
            centerX - rectHalfSize,
            centerY - rectHalfSize,
            centerX + rectHalfSize,
            centerY + rectHalfSize
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { drawLines(it) }
    }

    private fun drawLines(canvas: Canvas) {
        val path = Path()
        path.reset()

        path.moveTo(borderRect.left, borderRect.top + lineLength)
        path.lineTo(borderRect.left, borderRect.top)
        path.lineTo(borderRect.left + lineLength, borderRect.top)

        path.moveTo(borderRect.right - lineLength, borderRect.top)
        path.lineTo(borderRect.right, borderRect.top)
        path.lineTo(borderRect.right, borderRect.top + lineLength)

        path.moveTo(borderRect.right, borderRect.bottom - lineLength)
        path.lineTo(borderRect.right, borderRect.bottom)
        path.lineTo(borderRect.right - lineLength, borderRect.bottom)

        path.moveTo(borderRect.left + lineLength, borderRect.bottom)
        path.lineTo(borderRect.left, borderRect.bottom)
        path.lineTo(borderRect.left, borderRect.bottom - lineLength)

        canvas.drawPath(path, linePaint)
    }
}
