/*
 * Copyright (c) 2019 Salt Edge Inc.
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
