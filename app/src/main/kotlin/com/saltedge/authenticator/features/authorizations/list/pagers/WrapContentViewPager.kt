/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.authorizations.list.pagers

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager

private const val PADDING_TO_WIDTH_RATIO = 0.15

class WrapContentViewPager : ViewPager {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val actualWidth = MeasureSpec.getSize(widthMeasureSpec)
        val padding: Int = (actualWidth * PADDING_TO_WIDTH_RATIO).toInt()
        setPadding(padding, 0, padding, 0)
        clipToPadding = false
        pageMargin = 0
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}
