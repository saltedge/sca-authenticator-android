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
package com.saltedge.authenticator.features.launcher

import android.app.Activity
import android.content.Context
import androidx.lifecycle.*
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.features.main.MainActivity
import com.saltedge.authenticator.features.onboarding.OnboardingSetupActivity
import com.saltedge.authenticator.models.realm.RealmManagerAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.tools.AppTools
import com.saltedge.authenticator.tools.PasscodeToolsAbs

class LauncherViewModel(
    val appContext: Context,
    val preferenceRepository: PreferenceRepositoryAbs,
    val passcodeTools: PasscodeToolsAbs,
    val realmManager: RealmManagerAbs
) : ViewModel(), LifecycleObserver {

    var onDbInitializationFail = MutableLiveData<ViewModelEvent<Unit>>()
        private set

    var onInitializationSuccess = MutableLiveData<ViewModelEvent<Unit>>()
        private set

    var buttonClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
        private set

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onLifeCycleResume() {
        if (!AppTools.isTestsSuite(appContext)) realmManager.initRealm(context = appContext)
        if (realmManager.errorOccurred) {
            onInitializationSuccess.value = null
            onDbInitializationFail.postValue(ViewModelEvent(Unit))
        } else {
            if (shouldSetupApplication()) passcodeTools.replacePasscodeKey(appContext)
            onDbInitializationFail.value = null
            onInitializationSuccess.postValue(ViewModelEvent(Unit))
        }
    }

    fun getNextActivityClass(): Class<out Activity> =
        if (shouldSetupApplication()) OnboardingSetupActivity::class.java else MainActivity::class.java

    fun onOkClick() {
        realmManager.resetError()
        buttonClickEvent.postValue(ViewModelEvent(Unit))
    }

    private fun shouldSetupApplication(): Boolean = !preferenceRepository.passcodeExist()
}
