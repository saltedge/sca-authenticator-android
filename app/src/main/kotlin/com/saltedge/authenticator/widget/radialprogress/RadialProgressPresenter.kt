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

import android.graphics.RectF

class RadialProgressPresenter {

    var basePadding: Float = 0f
    var maxProgress: Int = 100 // Max progress in seconds
        set(value) {
            field = if (value < 0) 0 else value
        }
    var remainedProgress: Int = 0 // Progress in seconds
        set(value) {
            field = if (value < 0) 0 else if (value > maxProgress) maxProgress else value
        }
    var centerX = 0f
        private set
    var centerY = 0f
        private set
    var backgroundCircleRadius: Float = 0f
        private set
    var progressArcRectF: RectF = RectF()
        private set

    fun onViewSizeChanged(width: Int, height: Int) {
        centerX = width.toFloat() / 2f
        centerY = height.toFloat() / 2f
        backgroundCircleRadius = if (centerX < basePadding) 0f else centerX - basePadding
        val progressArcRadius =
            if (backgroundCircleRadius < basePadding) 0f else backgroundCircleRadius - basePadding
        progressArcRectF = RectF(
            centerX - progressArcRadius, centerY - progressArcRadius,
            centerX + progressArcRadius, centerY + progressArcRadius
        )
    }

    fun calculateSweepAngle(): Float =
        if (maxProgress == 0) 0f else 360f * remainedProgress.toFloat() / maxProgress

    fun calculateStartAngle(): Float = 270f - calculateSweepAngle()
}
