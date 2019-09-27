package com.saltedge.authenticator.features.connections.common

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView

class RoundedImageView : AppCompatImageView {

    private var circleBackgroundColor = Color.WHITE
    private var blackAndWhite: Boolean = false
    private var opacity: Int = 255
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

    fun setCircleBackgroundColor(@ColorInt color: Int) {
        circleBackgroundColor = color
        invalidate()
    }

    fun setEnabledColor() {
        opacity = 255
        blackAndWhite = false
        invalidate()
    }

    fun setDisabledColor() {
        opacity = 170
        blackAndWhite = true
        invalidate()
    }

    private fun Bitmap.toCircleBitmap(diameter: Int): Bitmap =
        emptyCircle(diameter).also {
//            val rect = Rect(0, 0, diameter, diameter)
            bgPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

            val scaleBitCrop = scaleAndCropBitmap(this, diameter.toFloat())
            val left = (it.width - scaleBitCrop.width) /2
            val top = (it.height - scaleBitCrop.height) /2
            Canvas(it).drawBitmap(scaleBitCrop, left.toFloat(), top.toFloat(), bgPaint)
            bgPaint.xfermode = null
        }

    private fun emptyCircle(diameter: Int): Bitmap =
        Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888).also {
            Canvas(it).drawCircle(diameter / 2f, diameter / 2f, diameter / 2f, bgPaint)
        }

    private fun scaleAndCropBitmap(bmp: Bitmap, fitSize: Float): Bitmap { //insert bmp in fitSize
        val scaleFactor = Math.max(bmp.width, bmp.height) / fitSize
        if (scaleFactor == 1F) return bmp // return just this func and remove scaleAndCropBitmap
        val scaledBitmap = Bitmap.createScaledBitmap(
            bmp, ((bmp.width / scaleFactor) + 0.5).toInt(),
            ((bmp.height / scaleFactor) + 0.5).toInt(), false
        )
        return Bitmap.createBitmap(
            scaledBitmap,
            ((scaledBitmap.width - fitSize) / 2).toInt(),
            ((scaledBitmap.height - fitSize) / 2).toInt(),
            fitSize.toInt(),
            fitSize.toInt()
        )
    }

    private fun createPaint(): Paint {
        return Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
            alpha = opacity
            color = circleBackgroundColor
            if (blackAndWhite) {
                colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
            }
        }
    }
}

