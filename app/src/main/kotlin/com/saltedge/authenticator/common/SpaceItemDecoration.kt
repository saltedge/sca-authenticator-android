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
package com.saltedge.authenticator.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.R

/**
 * Draw space between of list items
 */
class SpaceItemDecoration(
    context: Context,
    var headerPositions: Array<Int> = emptyArray()
) : RecyclerView.ItemDecoration() {

    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.divider_color)
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimension(R.dimen.dp_1)
    }

    private val spaceHeight = context.resources.getDimensionPixelSize(R.dimen.dp_20)
    private val dividerHeight = context.resources.getDimensionPixelSize(R.dimen.dp_1)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (needToDrawSpace(parent, view)) {
            outRect.top = spaceHeight
        }
        if (needToDrawDivider(parent, view)) {
            outRect.bottom = dividerHeight
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val dividerStart = parent.paddingStart
        val dividerEnd = parent.width - parent.paddingEnd
        val endIndex = parent.adapter?.itemCount ?: 0
        for (index in 0 until endIndex - 1) {
            val currentChild = parent.getChildAt(index)
            val startX = dividerStart.toFloat()
            val endX = dividerEnd.toFloat()
            val bottomY = currentChild.bottom.toFloat() + dividerHeight / 2
            if (needToDrawDivider(parent, currentChild)) {
                canvas.drawLine(startX, bottomY, endX, bottomY, dividerPaint)
            }
        }
    }

    /**
     * Determines at which positions the dividers should be drawn
     */
    private fun needToDrawDivider(parent: RecyclerView, view: View): Boolean {
        val viewPosition = parent.getChildAdapterPosition(view)
        return !headerPositions.contains(viewPosition + 1)
    }

    /**
     * Determines at which positions the header delimiters should be drawn
     */
    private fun needToDrawSpace(parent: RecyclerView, view: View): Boolean {
        val viewPosition = parent.getChildAdapterPosition(view)
        return headerPositions.contains(viewPosition)
    }
}
