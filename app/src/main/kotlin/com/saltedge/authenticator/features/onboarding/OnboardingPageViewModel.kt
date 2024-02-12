/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class OnboardingPageViewModel(
    @StringRes val titleResId: Int,
    @StringRes val subTitleResId: Int,
    @DrawableRes var imageResId: Int
)
