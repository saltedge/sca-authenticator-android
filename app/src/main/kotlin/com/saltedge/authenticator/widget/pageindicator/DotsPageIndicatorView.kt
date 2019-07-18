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
package com.saltedge.authenticator.widget.pageindicator

import android.annotation.TargetApi
import android.content.Context
import android.content.res.TypedArray
import android.database.DataSetObserver
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.util.Pair
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tool.applyAlphaToColor

const val DOTS_INDICATOR_ANIMATION_TIME = 350L

class DotsPageIndicatorView : View, ViewPager.OnPageChangeListener, ValueAnimation.UpdateListener {

    private val DEFAULT_UNSELECTED_COLOR = "#e5e5e5"
    private val DEFAULT_SELECTED_COLOR = "#b2b2b2"

    private val DEFAULT_CIRCLES_COUNT = 3
    private val COUNT_NOT_SET = -1

    private val DEFAULT_PADDING_DP = 4f

    private var radiusPx: Int = context.resources.getDimension(R.dimen.dp_4).toInt()
    private var paddingPx: Int = radiusPx * 2

    private var count = DEFAULT_CIRCLES_COUNT
    private var isCountSet: Boolean = false

    //Color
    private var selectedColor = Color.parseColor(DEFAULT_SELECTED_COLOR)
    private var inactiveColor: Int? = null
    private val unselectedColor: Int
        get() = inactiveColor ?: selectedColor.applyAlphaToColor(0.3f)

    //Worm
    private var frameLeftX: Int = 0
    private var frameRightX: Int = 0

    private var isFrameValuesSet: Boolean = false

    private var selectedPosition: Int = 0
    private var selectingPosition: Int = 0
    private var lastSelectedPosition: Int = 0

    private val interactiveAnimation = false
    private val animationDuration = DOTS_INDICATOR_ANIMATION_TIME

    private var setObserver: DataSetObserver? = null
    private var dynamicCount = true

    private val paint = Paint()
    private val rect = RectF()

    private var animation: ValueAnimation? = null

    private var viewPager: ViewPager? = null

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    override fun onDetachedFromWindow() {
        unRegisterSetObserver()
        super.onDetachedFromWindow()
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        val circleDiameterPx = radiusPx * 2
        val desiredHeight = circleDiameterPx
        var desiredWidth = 0

        if (count != 0) desiredWidth = circleDiameterPx * count + paddingPx * (count - 1)

        var width: Int = if (widthMode == View.MeasureSpec.EXACTLY) widthSize
        else if (widthMode == View.MeasureSpec.AT_MOST) Math.min(desiredWidth, widthSize)
        else desiredWidth

        var height: Int = if (heightMode == View.MeasureSpec.EXACTLY) heightSize
        else if (heightMode == View.MeasureSpec.AT_MOST) Math.min(desiredHeight, heightSize)
        else desiredHeight

        if (width < 0) width = 0
        if (height < 0) height = 0

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        setFrameValues()
    }

    override fun onDraw(canvas: Canvas) {
        drawIndicatorView(canvas)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (interactiveAnimation) onPageScroll(position, positionOffset)
    }

    override fun onPageSelected(position: Int) {
        if (!interactiveAnimation) selection = position
    }

    override fun onPageScrollStateChanged(state: Int) {/*empty*/}

    override fun onWormAnimationUpdated(leftX: Int, rightX: Int) {
        frameLeftX = leftX
        frameRightX = rightX
        invalidate()
    }

    /**
     * Set static number of circle indicators to be displayed.

     * @param count total count of indicators.
     */
    fun setCount(count: Int) {
        if (this.count != count) {
            this.count = count
            this.isCountSet = true

            requestLayout()
        }
    }

    /**
     * Return number of circle indicators
     */
    fun getCount(): Int {
        return count
    }

    /**
     * Dynamic count will automatically update number of circle indicators
     * if [ViewPager] page count updated on run-time.
     * Note: works if [ViewPager] set. See [.setViewPager].

     * @param dynamicCount boolean value to add/remove indicators dynamically.
     */
    fun setDynamicCount(dynamicCount: Boolean) {
        this.dynamicCount = dynamicCount
        if (dynamicCount) {
            registerSetObserver()
        } else {
            unRegisterSetObserver()
        }
    }

    /**
     * Set progress value in range [0 - 1] to specify step of animation while selecting new circle indicator.
     * @param selectingPosition selecting position with specific progress value.
     * @param progress          float value of progress.
     */
    fun setProgress(newPosition: Int, newProgress: Float) {
        if (interactiveAnimation) {
            this.selectingPosition = if (newPosition < 0) 0 else if (newPosition > count - 1) count - 1 else newPosition
            val progress = if (newProgress < 0) 0f else if (newProgress > 1) 1f else newProgress
            selectedAnimation.progress(progress)
        }
    }

    /**
     * Return position of currently selected circle indicator.
     */
    /**
     * Set specific circle indicator position to be selected. If position < or > total count,
     * accordingly first or last circle indicator will be selected.
     * @param selectedPosition position of indicator to select.
     */
    var selection: Int
        get() = selectedPosition
        set(value) {
            lastSelectedPosition = selectedPosition
            selectedPosition = if (value < 0) 0 else if (value > count - 1) count - 1 else value
            startWormAnimation()
        }

    /**
     * Set [ViewPager] to add [ViewPager.OnPageChangeListener] to automatically
     * handle selecting new indicators events (and interactive animation effect if it is enabled).

     * @param pager instance of [ViewPager] to work with
     */
    fun setViewPager(pager: ViewPager?) {
        if (pager != null) {
            viewPager = pager
            viewPager?.addOnPageChangeListener(this)

            setDynamicCount(dynamicCount)
            if (!isCountSet) setCount(viewPagerCount)
        }
    }

    private fun onPageScroll(position: Int, positionOffset: Float) {
        val progressPair = getProgress(position, positionOffset)
        val selectingPosition = progressPair.first
        val selectingProgress = progressPair.second

        if (selectingProgress == 1f) {
            lastSelectedPosition = selectedPosition
            selectedPosition = selectingPosition
        }

        setProgress(selectingPosition, selectingProgress)
    }

    private fun drawIndicatorView(canvas: Canvas) {
        val y = height / 2
        for (i in 0 until count) {
            val x = getXCoordinate(i)
            drawCircle(canvas, i, x, y)
        }
    }

    private fun drawCircle(canvas: Canvas, position: Int, x: Int, y: Int) {
        val selectedItem = !interactiveAnimation && (position == selectedPosition || position == lastSelectedPosition)
        val selectingItem = interactiveAnimation && (position == selectingPosition || position == selectedPosition)
        if (selectedItem or selectingItem) drawWithAnimationEffect(canvas, x.toFloat(), y.toFloat())
        else drawWithNoEffect(canvas, position, x.toFloat(), y.toFloat())
    }

    private fun drawWithAnimationEffect(canvas: Canvas, x: Float, y: Float) = drawWithWormAnimation(canvas, x, y)

    private fun drawWithWormAnimation(canvas: Canvas, x: Float, y: Float) {
        val dotRadius = radiusPx
        rect.set(frameLeftX.toFloat(), y - dotRadius, frameRightX.toFloat(), y + dotRadius)

        paint.color = unselectedColor
        canvas.drawCircle(x, y, dotRadius.toFloat(), paint)

        paint.color = selectedColor
        canvas.drawRoundRect(rect, dotRadius.toFloat(), dotRadius.toFloat(), paint)
    }

    private fun drawWithNoEffect(canvas: Canvas, position: Int, x: Float, y: Float) {
        paint.color = if (position == selectedPosition) selectedColor else unselectedColor
        canvas.drawCircle(x, y, radiusPx.toFloat(), paint)
    }

    private fun init(attrs: AttributeSet?) {
        initAttributes(attrs)
        initAnimation()
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
    }

    private fun initAttributes(attrs: AttributeSet?) {
        if (attrs == null) return
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.DotsPageIndicatorView, 0, 0)
        try {
            initCountAttribute(attributes)
            initColorAttribute(attributes)
        } finally {
            attributes.recycle()
        }
    }

    private fun initCountAttribute(typedArray: TypedArray) {
        setDynamicCount(dynamicCount)

        count = COUNT_NOT_SET
        if (count != COUNT_NOT_SET) isCountSet = true
        else count = DEFAULT_CIRCLES_COUNT

        var position = typedArray.getInt(R.styleable.DotsPageIndicatorView_select, 0)
        if (position < 0) position = 0
        else if (count > 0 && position > count - 1) position = count - 1

        selectedPosition = position
        selectingPosition = position
    }

    private fun initColorAttribute(typedArray: TypedArray) {
        if (typedArray.hasValue(R.styleable.DotsPageIndicatorView_unselectedColor))
            inactiveColor = typedArray.getColor(R.styleable.DotsPageIndicatorView_unselectedColor, Color.parseColor(DEFAULT_UNSELECTED_COLOR))
        selectedColor = typedArray.getColor(R.styleable.DotsPageIndicatorView_selectedColor, selectedColor)
    }

    private fun initAnimation() {
        animation = ValueAnimation(this)
    }

    private fun setFrameValues() {
        if (isFrameValuesSet) {
            return
        }

        //worm
        val xCoordinate = getXCoordinate(selectedPosition)
        if (xCoordinate - radiusPx >= 0) {
            frameLeftX = xCoordinate - radiusPx
            frameRightX = xCoordinate + radiusPx

        } else {
            frameLeftX = xCoordinate
            frameRightX = xCoordinate + radiusPx * 2
        }

        isFrameValuesSet = true
    }

    private fun startWormAnimation() {
        val fromX = getXCoordinate(lastSelectedPosition)
        val toX = getXCoordinate(selectedPosition)

        val isRightSide = selectedPosition > lastSelectedPosition

        animation?.worm()?.end()
        animation?.worm()?.with(fromX, toX, radiusPx, isRightSide)?.duration(animationDuration)?.start()
    }

    private val selectedAnimation: AbsAnimation<*>
        get() {
            val fromX = getXCoordinate(selectedPosition)
            val toX = getXCoordinate(selectingPosition)

            val isRightSide = selectingPosition > selectedPosition
            return animation!!.worm().with(fromX, toX, radiusPx, isRightSide)

        }

    private fun registerSetObserver() {
        if (setObserver == null && viewPager != null && viewPager?.adapter != null) {
            setObserver = object : DataSetObserver() {
                override fun onChanged() {
                    super.onChanged()
                    viewPager?.adapter?.count?.let { setCount(it) }
                }
            }
            setObserver?.let { viewPager?.adapter?.registerDataSetObserver(it) }
        }
    }

    private fun unRegisterSetObserver() {
        setObserver?.let { viewPager?.adapter?.unregisterDataSetObserver(it)  }
        setObserver = null
    }

    private val viewPagerCount: Int
        get() = viewPager?.adapter?.count ?: count

    private fun getXCoordinate(position: Int): Int {
        var x = 0
        for (i in 0 until count) {
            x += radiusPx
            if (position == i) return x
            x += radiusPx + paddingPx
        }
        return x
    }

    private fun getProgress(position: Int, positionOffset: Float): Pair<Int, Float> {
        val isRightOverScrolled = position > selectedPosition
        val isLeftOverScrolled = position + 1 < selectedPosition

        if (isRightOverScrolled || isLeftOverScrolled) selectedPosition = position

        val isSlideToRightSide = selectedPosition == position && positionOffset != 0f
        val selectingPosition: Int
        var selectingProgress: Float

        if (isSlideToRightSide) {
            selectingPosition = position + 1
            selectingProgress = positionOffset
        } else {
            selectingPosition = position
            selectingProgress = 1 - positionOffset
        }

        if (selectingProgress > 1) selectingProgress = 1f
        else if (selectingProgress < 0) selectingProgress = 0f

        return Pair(selectingPosition, selectingProgress)
    }

    private fun calculateActualViewWidth(): Int {
        var width = 0
        val diameter = radiusPx * 2

        for (i in 0 until count) {
            width += diameter
            if (i < count - 1) width += paddingPx
        }
        return width
    }
}
