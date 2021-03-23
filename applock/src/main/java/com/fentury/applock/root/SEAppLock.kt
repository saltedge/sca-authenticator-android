/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.root

import android.content.Context
import com.fentury.applock.repository.PreferencesRepository
import com.fentury.applock.root.di.DaggerPasscodeComponent
import com.fentury.applock.root.di.PasscodeComponent
import com.fentury.applock.root.di.PasscodeModule
import com.fentury.applock.tools.PasscodeTools

class SEAppLock {

    companion object {
        internal var applicationName: String? = null
        internal var actionButtonText: String? = null
        internal var passcodeDescriptionText: String? = null
        internal lateinit var passcodeComponent: PasscodeComponent

        fun initAppLock(
            context: Context, appName: String, forgotActionButtonText: String, otpDescriptionText: String
        ) {
            PreferencesRepository.initObject(context)
            if (!PreferencesRepository.passcodeExist()) PasscodeTools.replacePasscodeKey(context = context)
            applicationName = appName
            actionButtonText = forgotActionButtonText
            passcodeDescriptionText = otpDescriptionText

            passcodeComponent = DaggerPasscodeComponent.builder()
                    .passcodeModule(PasscodeModule(context))
                    .build()
        }

        fun clearPasscodePreferences() {
            PreferencesRepository.clearPasscodePreferences()
        }
    }
}