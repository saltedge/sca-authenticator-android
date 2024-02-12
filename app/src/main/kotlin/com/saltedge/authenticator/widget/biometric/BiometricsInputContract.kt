/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.widget.biometric

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
