package com.saltedge.authenticator.features.authorizations.list

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager

class WrapContentViewPager: ViewPager {

    init {
        setPageTransformer(true, CarouselPagerTransformer(this))
    }

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

    companion object{
        const val PADDING_TO_WIDTH_RATIO = 0.18
    }
}
