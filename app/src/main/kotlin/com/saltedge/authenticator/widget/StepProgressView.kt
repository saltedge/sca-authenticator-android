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
package com.saltedge.authenticator.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tool.log

private const val KEY_SUPER_STATE = "superState"
private const val KEY_ANIMATION_TIME = "stepAnimationTime"
private const val KEY_STEP_COUNT = "stepCount"
private const val KEY_CURRENT_STEP = "currentStep"
private const val DEFAULT_STEP_ANIMATION_TIME = 255f

class StepProgressView : View, ValueAnimator.AnimatorUpdateListener {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val outerCircleSize: Int
        get() = context.resources.getDimension(R.dimen.dp_12).toInt()
    private val outerCircleRadius: Float
        get() = context.resources.getDimension(R.dimen.dp_6)
    private val innerCircleRadius: Float
        get() = context.resources.getDimension(R.dimen.dp_5) / 2
    private val lineHeight: Float
        get() = context.resources.getDimension(R.dimen.dp_2)
    private val futureStepsColor: Int
        get() = ContextCompat.getColor(context, R.color.gray_light)
    private val passedStepColor: Int
        get() = ContextCompat.getColor(context, R.color.blue)
    private val passedStepCenterPointColor: Int
        get() = ContextCompat.getColor(context, R.color.color_secondary)
    private var linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = lineHeight
    }
    private var pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = lineHeight
    }
    private var centerY = 0f
    private var baseLineMaxWidth = 0f
    private var valueAnimator: ValueAnimator? = null
    private var animatedStepValue = 0f
    private var currentStep = 0f
    private var stepAnimationTime = DEFAULT_STEP_ANIMATION_TIME
    var stepCount: Int = 0
        set(value) {
            field = if (value < 0) 0 else value
            currentStep = 0f
            invalidate()
        }
    private val maxStep: Float
        get() = if (stepCount < 0) 0f else stepCount.toFloat() - 1f

    fun stepForward() {
        setStepProgress(Math.floor(currentStep.toDouble()).toFloat() + 1f)
    }

    fun stepBack() {
        setStepProgress(Math.ceil(currentStep.toDouble()).toFloat() - 1f)
    }

    fun addStepProgress(offset: Float) {
        setStepProgress(currentStep + offset)
    }

    fun setStepProgress(step: Float) {
        val startStepValue = currentStep
        currentStep = if (step > maxStep) maxStep else if (step < 0f) 0f else step
        drawChanges(startStepValue, currentStep)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, outerCircleSize)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        baseLineMaxWidth = if (w == 0) 0f else (w - outerCircleSize).toFloat()
        centerY = h.toFloat() / 2
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState())
        bundle.putFloat(KEY_ANIMATION_TIME, this.stepAnimationTime)
        bundle.putInt(KEY_STEP_COUNT, this.stepCount)
        bundle.putFloat(KEY_CURRENT_STEP, this.currentStep)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            this.stepAnimationTime = state.getFloat(KEY_ANIMATION_TIME)
            this.stepCount = state.getInt(KEY_STEP_COUNT)
            this.currentStep = state.getFloat(KEY_CURRENT_STEP)
            this.animatedStepValue = this.currentStep
            super.onRestoreInstanceState(state.getParcelable(KEY_SUPER_STATE))
        } else super.onRestoreInstanceState(state)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (stepCount <= 1) return
        canvas?.let {
            drawLines(it)
            drawPoints(it)
        }
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        animatedStepValue = valueAnimator?.animatedValue as? Float ?: currentStep
        this.invalidate()
    }

    private fun drawLines(canvas: Canvas) {
        linePaint.color = futureStepsColor
        val lineStartY = outerCircleRadius
        canvas.drawLine(lineStartY, centerY, baseLineMaxWidth, centerY, linePaint)
        val activeProgressWidth = baseLineMaxWidth * animatedStepValue / maxStep
        if (activeProgressWidth > 0) {
            val lineEndY = outerCircleRadius + activeProgressWidth
            canvas.drawLine(
                lineStartY,
                centerY,
                lineEndY,
                centerY,
                linePaint.apply { color = passedStepColor })
        }
    }

    private fun drawPoints(canvas: Canvas) {
        (0 until stepCount).forEach {
            val stepIndex = it.toFloat()
            val circleX = outerCircleRadius + baseLineMaxWidth * stepIndex / maxStep
            when {
                (animatedStepValue > stepIndex || animatedStepValue.toInt() == stepCount - 1) -> {
                    drawCompletedActiveStepPoint(canvas, circleX)
                }
                animatedStepValue == stepIndex -> {
                    drawCurrentActiveStepPoint(canvas, circleX)
                }
                else -> {
                    drawInactiveStepPoint(canvas, circleX)
                }
            }
        }
    }

    private fun drawCurrentActiveStepPoint(canvas: Canvas, circleX: Float) {
        canvas.drawCircle(
            circleX,
            centerY,
            outerCircleRadius,
            pointPaint.apply { color = passedStepColor })
        canvas.drawCircle(
            circleX,
            centerY,
            outerCircleRadius - lineHeight,
            pointPaint.apply { color = Color.WHITE })
    }

    private fun drawCompletedActiveStepPoint(canvas: Canvas, circleX: Float) {
        drawCurrentActiveStepPoint(canvas, circleX)
        canvas.drawCircle(
            circleX,
            centerY,
            innerCircleRadius,
            pointPaint.apply { color = passedStepCenterPointColor })
    }

    private fun drawInactiveStepPoint(canvas: Canvas, circleX: Float) {
        canvas.drawCircle(
            circleX,
            centerY,
            outerCircleRadius,
            pointPaint.apply { color = futureStepsColor })
    }

    private fun drawChanges(startStepValue: Float, endStepValue: Float) {
        try {
            valueAnimator?.cancel()
            val diff = endStepValue - startStepValue
            if (diff == 0f) {
                animatedStepValue = endStepValue
                this.invalidate()
            } else {
                valueAnimator = ValueAnimator.ofFloat(startStepValue, endStepValue)
                valueAnimator?.duration = (Math.abs(diff) * stepAnimationTime).toLong()
                valueAnimator?.interpolator = DecelerateInterpolator()
                valueAnimator?.addUpdateListener(this)
                valueAnimator?.start()
            }
        } catch (e: Exception) {
            e.log()
        }
    }
}
