/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
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
package com.saltedge.authenticator.features.launcher

import android.content.Context
import androidx.lifecycle.*
import com.fentury.applock.tools.PasscodeTools
import com.saltedge.authenticator.R
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.realm.RealmManagerAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.tools.PasscodeToolsAbs

class SplashViewModel(
    val appContext: Context,
    val preferenceRepository: PreferenceRepositoryAbs,
    val passcodeTools: PasscodeToolsAbs,
    val realmManager: RealmManagerAbs
) : ViewModel(), LifecycleObserver {

    val showNextScreen = MutableLiveData<ViewModelEvent<Int>>()
    private val shouldShowOnboarding: Boolean
        get() = PasscodeTools.getPasscode().isEmpty()

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onLifeCycleResume() {
        if (shouldShowOnboarding) showNextScreen.postValue(ViewModelEvent(R.id.onboardingFragment))
        else showNextScreen.postValue(ViewModelEvent(R.id.authorizationsListFragment))
    }
}
