/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.widget.pageindicator

class ValueAnimation(private val updateListener: ValueAnimation.UpdateListener?) {

    private var wormAnimation: WormAnimation? = null

    interface UpdateListener {

        fun onWormAnimationUpdated(leftX: Int, rightX: Int)
    }

    fun worm(): WormAnimation {
        if (wormAnimation == null) {
            wormAnimation = WormAnimation(updateListener!!)
        }

        return wormAnimation as WormAnimation
    }
}
