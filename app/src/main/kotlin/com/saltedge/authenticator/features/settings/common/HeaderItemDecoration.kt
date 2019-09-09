package com.saltedge.authenticator.features.settings.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.R

class HeaderItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val spacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.gray_extra_light)
        style = Paint.Style.FILL
    }
    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.divider_color)
        style = Paint.Style.FILL
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
                val params = currentChild.layoutParams as RecyclerView.LayoutParams
                val topView = currentChild.top + params.topMargin
                val startX = dividerStart.toFloat()
                val topY = topView.toFloat() - rectHeight
                val endX = dividerEnd.toFloat()
                val bottomY = topView.toFloat()
                canvas.drawRect(startX, topY, endX, bottomY, spacePaint)
                canvas.drawLine(startX, bottomY - 1, endX, bottomY - 1, dividerPaint)
            }
        }
    }

    private fun needDrawBottomDelimiter(parent: RecyclerView, view: View): Boolean {
        val viewPosition = parent.getChildAdapterPosition(view)
        return viewPosition == 0 || viewPosition == 3
    }
}
