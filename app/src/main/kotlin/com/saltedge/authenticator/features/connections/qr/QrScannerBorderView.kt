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
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.saltedge.authenticator.R

class QrScannerBorderView : View {

    private var qrRect = RectF()
    private var titleXPos = 0F
    private var titleYPos = 0F
    private var descriptionYPos = 0F

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { drawQrItems(it) }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val rectHalfSize = Math.min(w, h) * 0.7f / 2
        val centerX = w.toFloat() / 2
        val centerY = h.toFloat() / 2
        qrRect = RectF(
            centerX - rectHalfSize,
            centerY - rectHalfSize,
            centerX + rectHalfSize,
            centerY + rectHalfSize
        )

        titleXPos = centerX
        titleYPos = centerY - rectHalfSize * 2
        descriptionYPos = centerY - rectHalfSize * 2 + 120F //bad number
    }

    private fun drawQrItems(canvas: Canvas) {
        val paint = Paint()

        drawQrTitle(canvas = canvas, paint = paint)
        drawQrDescription(canvas = canvas, paint = paint)
        drawQrRect(canvas = canvas, paint = paint)
    }

    private fun drawQrTitle(canvas: Canvas, paint: Paint) {
        val customTypeface = ResourcesCompat.getFont(context, R.font.roboto_bold)

        paint.color = context.resources.getColor(R.color.primary_text)
        paint.textSize = context.resources.getDimension(R.dimen.text_24)
        paint.typeface = customTypeface
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(context.getString(R.string.scan_qr_title), titleXPos, titleYPos, paint)
    }

    private fun drawQrDescription(canvas: Canvas, paint: Paint) {
        val customTypeface = ResourcesCompat.getFont(context, R.font.roboto_regular)

        paint.color = context.resources.getColor(R.color.primary_text)
        paint.textSize = context.resources.getDimension(R.dimen.text_18)
        paint.typeface = customTypeface
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(context.getString(R.string.scan_qr_description), titleXPos, descriptionYPos, paint)
    }

    private fun drawQrRect(canvas: Canvas, paint: Paint) {
        paint.color = Color.TRANSPARENT
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawRect(qrRect, paint)
    }
}
