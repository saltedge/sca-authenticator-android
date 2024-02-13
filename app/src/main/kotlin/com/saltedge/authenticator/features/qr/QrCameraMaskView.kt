/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.qr

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.saltedge.authenticator.tools.convertDpToPx

class QrCameraMaskView : View {
    private val rectHalfSize = convertDpToPx(232F) / 2
    private val roundRadius = convertDpToPx(4F).toFloat()
    private var qrRect = RectF()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawQrRect(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val centerX = w.toFloat() / 2
        val centerY = h.toFloat() / 2
        qrRect = RectF(
            centerX - rectHalfSize,
            centerY - rectHalfSize,
            centerX + rectHalfSize,
            centerY + rectHalfSize
        )
    }

    private fun drawQrRect(canvas: Canvas) {
        val paint = Paint()

        paint.color = Color.TRANSPARENT
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawRoundRect(qrRect, roundRadius, roundRadius, paint)
    }
}
