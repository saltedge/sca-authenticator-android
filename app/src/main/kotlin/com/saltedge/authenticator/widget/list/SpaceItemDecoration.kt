/*
 * Copyright (c) 2020 Salt Edge Inc.
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
    var headerPositions: Array<Int> = emptyArray(),
    var footerPositions: Array<Int> = emptyArray()
) : RecyclerView.ItemDecoration() {

    private val spaceHeight = context.resources.getDimensionPixelSize(R.dimen.dp_20)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val viewPosition = parent.getChildAdapterPosition(view)
        if (needToDrawHeaderSpace(viewPosition)) outRect.top = spaceHeight
        if (needToDrawFooterSpace(viewPosition)) outRect.bottom = spaceHeight
    }

    fun setHeaderForAllItems(itemCount: Int) {
        if (itemCount > 0) {
            headerPositions = (0..itemCount).toList().toTypedArray()
        }
    }

    /**
     * Determines at which positions the header delimiters should be drawn
     */
    private fun needToDrawHeaderSpace(viewPosition: Int): Boolean {
        return headerPositions.contains(viewPosition)
    }

    /**
     * Determines at which positions the footer delimiters should be drawn
     */
    private fun needToDrawFooterSpace(viewPosition: Int): Boolean {
        return footerPositions.contains(viewPosition)
    }
}
