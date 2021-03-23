/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.widget.biometric

interface BiometricsInputContract {

    interface View {
        fun updateStatusView(
            imageResId: Int,
            textColorResId: Int,
            textResId: Int,
            animateText: Boolean
        )

        fun sendAuthSuccessResult()
    }
}