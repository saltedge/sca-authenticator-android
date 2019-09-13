package com.saltedge.authenticator.features.settings.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.R

/**
 * Draw large delimiters between some of settings items
 */
class HeaderItemDecoration(context: Context, private val delimiterPositions: Array<Int>) : RecyclerView.ItemDecoration() {

    private val spacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.gray_extra_light)
        style = Paint.Style.FILL
    }
    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.divider_color)
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimension(R.dimen.dp_1)
    }

    private val rectHeight = context.resources.getDimensionPixelSize(R.dimen.dp_20)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (needDrawBottomDelimiter(parent, view)) {
            outRect.top = rectHeight
        } else {
            super.getItemOffsets(outRect, view, parent, state)
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val dividerStart = parent.paddingStart
        val dividerEnd = parent.width - parent.paddingEnd
        val endIndex = parent.adapter?.itemCount ?: 0
        for (index in 0 until endIndex) {
            val currentChild = parent.getChildAt(index)
            if (needDrawBottomDelimiter(parent, currentChild)) {
                val topOfCurrentView = currentChild.top
                val startX = dividerStart.toFloat()
                val topY = topOfCurrentView.toFloat() - rectHeight
                val endX = dividerEnd.toFloat()
                val bottomY = topOfCurrentView.toFloat()
                canvas.drawRect(startX, topY, endX, bottomY, spacePaint)
                canvas.drawLine(startX, bottomY, endX, bottomY, dividerPaint)
            }
        }
    }

    /**
     * Determines at which positions the header delimiters should be drawn
     */
    private fun needDrawBottomDelimiter(parent: RecyclerView, view: View): Boolean {
        val viewPosition = parent.getChildAdapterPosition(view)
        return delimiterPositions.contains(viewPosition)
    }
}
