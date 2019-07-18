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

import android.animation.Animator
import android.animation.AnimatorSet

abstract class AbsAnimation<T : Animator>(protected var listener: ValueAnimation.UpdateListener) {

    protected var animationDuration = DOTS_INDICATOR_ANIMATION_TIME
    protected var animator: T? = null

    init {
        animator = this.createAnimator()
    }

    abstract fun createAnimator(): T

    abstract fun progress(progress: Float): AbsAnimation<*>

    fun duration(duration: Long): AbsAnimation<*> {
        animationDuration = duration

        if (animator is AnimatorSet) {
            val size = (animator as AnimatorSet).childAnimations.size
            val singleDuration = animationDuration / size
            animator?.duration = singleDuration
        } else {
            animator?.duration = animationDuration
        }

        return this
    }

    fun start() {
        animator?.start()
    }

    fun end() {
        animator?.end()
    }
}
