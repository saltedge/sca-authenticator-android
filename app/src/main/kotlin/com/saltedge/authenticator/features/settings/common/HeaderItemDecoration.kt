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
 * Draw large delimiters between of settings items
 */
class HeaderItemDecoration(
    context: Context,
    var headerPositions: Array<Int>
) : RecyclerView.ItemDecoration() {

    private val spacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.gray_extra_light)
        style = Paint.Style.FILL
    }

    private val rectHeight = context.resources.getDimensionPixelSize(R.dimen.dp_20)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (needDrawHeader(parent, view)) {
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
            if (needDrawHeader(parent, currentChild)) {
                val topOfCurrentView = currentChild.top
                val startX = dividerStart.toFloat()
                val topY = topOfCurrentView.toFloat() - rectHeight
                val endX = dividerEnd.toFloat()
                val bottomY = topOfCurrentView.toFloat()
                canvas.drawRect(startX, topY, endX, bottomY, spacePaint)
            }
        }
    }

    /**
     * Determines at which positions the header delimiters should be drawn
     */
    private fun needDrawHeader(parent: RecyclerView, view: View): Boolean {
        val viewPosition = parent.getChildAdapterPosition(view)
        return headerPositions.contains(viewPosition)
    }
}
