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
package com.saltedge.authenticator.features.launcher

import android.app.Activity
import com.saltedge.authenticator.features.main.MainActivity
import com.saltedge.authenticator.features.onboarding.OnboardingSetupActivity
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.tools.biometric.BiometricToolsAbs
import com.saltedge.authenticator.tool.secure.PasscodeToolsAbs
import javax.inject.Inject

class LauncherPresenter @Inject constructor(
    private val preferenceRepository: PreferenceRepositoryAbs,
    private val passcodeTools: PasscodeToolsAbs,
    private val biometricTools: BiometricToolsAbs
) {

    fun setupApplication() {
        if (shouldSetupApplication()) {
            passcodeTools.replacePasscodeKey()
            biometricTools.replaceFingerprintKey()
        }
    }

    fun getNextActivityClass(): Class<out Activity> =
        if (shouldSetupApplication()) OnboardingSetupActivity::class.java else MainActivity::class.java

    private fun shouldSetupApplication(): Boolean = !preferenceRepository.passcodeExist()
}
