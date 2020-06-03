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
package com.saltedge.authenticator.widget.list

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.R

/**
 * Draw space between of list items
 */
class SpaceItemDecoration(
    context: Context,
    var headerPositions: Array<Int> = emptyArray()
) : RecyclerView.ItemDecoration() {

    private val spaceHeight = context.resources.getDimensionPixelSize(R.dimen.dp_20)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (needToDrawSpace(parent, view)) {
            outRect.top = spaceHeight
        }
    }

    /**
     * Determines at which positions the header delimiters should be drawn
     */
    private fun needToDrawSpace(parent: RecyclerView, view: View): Boolean {
        val viewPosition = parent.getChildAdapterPosition(view)
        return headerPositions.contains(viewPosition)
    }
}
