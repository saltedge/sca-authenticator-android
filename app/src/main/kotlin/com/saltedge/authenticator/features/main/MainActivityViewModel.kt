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
package com.saltedge.authenticator.features.main

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.model.realm.RealmManagerAbs
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.tool.secure.PasscodeToolsAbs

class MainActivityViewModel(
    val appContext: Context,
    val preferenceRepository: PreferenceRepositoryAbs,
    val passcodeTools: PasscodeToolsAbs,
    val realmManager: RealmManagerAbs
) : ViewModel(), LifecycleObserver {

    init {
        if (!realmManager.initialized) realmManager.initRealm(context = appContext)
    }
//
//    var buttonClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
//        private set

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onLifeCycleResume() {


    }

//    fun getNextActivityClass(): Class<out Activity> =
//        if (shouldSetupApplication()) OnboardingSetupActivity::class.java else MainActivity::class.java
//
//    fun onOkClick() {
//        realmManager.resetError()
//        buttonClickEvent.postValue(ViewModelEvent())
//    }
//
//    private fun shouldSetupApplication(): Boolean = !preferenceRepository.passcodeExist()
}
