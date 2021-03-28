/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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
package com.saltedge.authenticator.features.onboarding

import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.R
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.tools.PasscodeToolsAbs
import com.saltedge.authenticator.tools.postUnitEvent

class OnboardingSetupViewModel(
    val appContext: Context,
    val passcodeTools: PasscodeToolsAbs,
    val preferenceRepository: PreferenceRepositoryAbs
) : ViewModel(),
    LifecycleObserver
{
    val proceedViewVisibility: MutableLiveData<Int> = MutableLiveData(View.GONE)
    val skipViewVisibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE)
    val pageIndicator = MutableLiveData<Int>()
    val moveNext = MutableLiveData<ViewModelEvent<Unit>>()
    val showPasscodeSetup = MutableLiveData<ViewModelEvent<Unit>>()

    val onboardingViewModels: List<OnboardingPageViewModel> = listOf(
        OnboardingPageViewModel(
            titleResId = R.string.onboarding_carousel_one_title,
            subTitleResId = R.string.onboarding_carousel_one_description,
            imageResId = R.drawable.ic_onboarding_page_1
        ),
        OnboardingPageViewModel(
            titleResId = R.string.onboarding_carousel_two_title,
            subTitleResId = R.string.onboarding_carousel_two_description,
            imageResId = R.drawable.ic_onboarding_page_2
        ),
        OnboardingPageViewModel(
            titleResId = R.string.onboarding_carousel_three_title,
            subTitleResId = R.string.onboarding_carousel_three_description,
            imageResId = R.drawable.ic_onboarding_page_3
        )
    )

    fun onOnboardingPageSelected(position: Int) {
        if (onboardingViewModels.getOrNull(position) != null) {
            pageIndicator.postValue(position)
            if (position == onboardingViewModels.lastIndex) {
                proceedViewVisibility.postValue(View.VISIBLE)
                skipViewVisibility.postValue(View.GONE)
            }
        }
    }

    fun onViewClick(viewId: Int) {
        when (viewId) {
            R.id.skipActionView, R.id.proceedToSetup -> showPasscodeSetup.postUnitEvent()
            R.id.nextActionView -> moveNext.postUnitEvent()
        }
    }
}
