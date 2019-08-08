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

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator

class WormAnimation(listener: ValueAnimation.UpdateListener) : AbsAnimation<AnimatorSet>(listener) {

    private var fromValue: Int = 0
    private var toValue: Int = 0
    private var radius: Int = 0
    private var isRightSide: Boolean = false

    private var rectLeftX: Int = 0
    private var rectRightX: Int = 0

    override fun createAnimator(): AnimatorSet {
        val animator = AnimatorSet()
        animator.interpolator = DecelerateInterpolator()

        return animator
    }

    fun with(fromValue: Int, toValue: Int, radius: Int, isRightSide: Boolean): WormAnimation {
        if (hasChanges(fromValue, toValue, radius, isRightSide)) {
            animator = createAnimator()

            this.fromValue = fromValue
            this.toValue = toValue
            this.radius = radius
            this.isRightSide = isRightSide

            val values = createAnimationValues(isRightSide)
            val straightAnimator = createValueAnimator(values.fromX, values.toX, false)
            val reverseAnimator = createValueAnimator(values.reverseFromX, values.reverseToX, true)

            animator?.playSequentially(straightAnimator, reverseAnimator)
        }
        return this
    }

    override fun progress(progress: Float): WormAnimation {
        if (animator != null) {
            var playTimeLeft = (progress * animationDuration).toLong()

            for (anim in animator!!.childAnimations) {
                val animator = anim as ValueAnimator

                if (playTimeLeft < 0) {
                    playTimeLeft = 0
                }

                var currPlayTime = playTimeLeft
                if (currPlayTime >= animator.duration) {
                    currPlayTime = animator.duration
                }

                animator.currentPlayTime = currPlayTime
                playTimeLeft -= currPlayTime
            }
        }

        return this
    }

    private fun createValueAnimator(fromX: Int, toX: Int, isReverseAnimator: Boolean): ValueAnimator {
        val anim = ValueAnimator.ofInt(fromX, toX)
        anim.duration = animationDuration
        anim.addUpdateListener { animation ->
            val value = animation.animatedValue as Int

            if (!isReverseAnimator) {
                if (isRightSide) {
                    rectRightX = value
                } else {
                    rectLeftX = value
                }
            } else {
                if (isRightSide) {
                    rectLeftX = value
                } else {
                    rectRightX = value
                }
            }

            listener.onWormAnimationUpdated(rectLeftX, rectRightX)
        }

        return anim
    }

    @SuppressWarnings("RedundantIfStatement")
    private fun hasChanges(fromValue: Int, toValue: Int, radius: Int, isRightSide: Boolean): Boolean {
        return this.fromValue != fromValue || this.toValue != toValue || this.radius != radius || this.isRightSide != isRightSide
    }

    private fun createAnimationValues(isRightSide: Boolean): AnimationValues {
        val fromX: Int
        val toX: Int

        val reverseFromX: Int
        val reverseToX: Int

        if (isRightSide) {
            fromX = fromValue + radius
            toX = toValue + radius

            reverseFromX = fromValue - radius
            reverseToX = toValue - radius
        } else {
            fromX = fromValue - radius
            toX = toValue - radius

            reverseFromX = fromValue + radius
            reverseToX = toValue + radius
        }

        return AnimationValues(fromX, toX, reverseFromX, reverseToX)
    }

    private inner class AnimationValues(val fromX: Int, val toX: Int, val reverseFromX: Int, val reverseToX: Int)
}