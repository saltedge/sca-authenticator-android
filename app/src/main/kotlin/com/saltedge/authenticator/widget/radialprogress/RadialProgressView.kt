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
package com.saltedge.authenticator.widget.radialprogress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.saltedge.authenticator.R

private const val KEY_SUPER_STATE = "superState"
private const val KEY_MAX_PROGRESS = "max"
private const val KEY_CURRENT_PROGRESS = "currentProgress"

class RadialProgressView : View {

    private val presenter = RadialProgressPresenter()
    private val backgroundCirclePaint: Paint
        get() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = ContextCompat.getColor(context, R.color.cyan_light)
        }
    private val progressArcPaint: Paint
        get() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = ContextCompat.getColor(context, R.color.blue)
        }

    init {
        presenter.basePadding = context.resources.getDimension(R.dimen.dp_2)
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var max: Int
        get() = presenter.maxProgress
        set(value) {
            presenter.maxProgress = value
        }
    var remainedProgress: Int
        get() = presenter.remainedProgress
        set(value) {
            presenter.remainedProgress = value
            invalidate()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = View.MeasureSpec.getSize(heightMeasureSpec)
        val size: Int = Math.min(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        presenter.onViewSizeChanged(w, h)
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState())
        bundle.putInt(KEY_MAX_PROGRESS, this.max)
        bundle.putInt(KEY_CURRENT_PROGRESS, this.remainedProgress)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            this.remainedProgress = state.getInt(KEY_CURRENT_PROGRESS)
            this.max = state.getInt(KEY_MAX_PROGRESS)
            super.onRestoreInstanceState(state.getParcelable(KEY_SUPER_STATE))
        } else super.onRestoreInstanceState(state)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.drawCircle(presenter.centerX, presenter.centerY,
                    presenter.backgroundCircleRadius, backgroundCirclePaint)
            it.drawArc(presenter.progressArcRectF, presenter.calculateStartAngle(),
                    presenter.calculateSweepAngle(), true, progressArcPaint)
        }
    }
}
