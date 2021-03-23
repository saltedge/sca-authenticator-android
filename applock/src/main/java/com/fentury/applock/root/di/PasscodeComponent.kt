/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.root.di

import android.content.Context
import com.fentury.applock.passcode.setup.PasscodeSetupFragment
import com.fentury.applock.root.ViewModelsFactory
import com.fentury.applock.lock.LockableActivity
import com.fentury.applock.passcode.edit.PasscodeEditFragment
import com.fentury.applock.repository.PreferencesRepositoryAbs
import com.fentury.applock.tools.PasscodeToolsAbs
import com.fentury.applock.tools.biometric.BiometricToolsAbs
import com.fentury.applock.tools.keystore.KeyStoreManagerAbs
import com.fentury.applock.widget.biometric.BiometricPromptAbs
import dagger.Component
import javax.inject.Scope

@Component(modules = [PasscodeModule::class])
@PasscodeScope
internal interface PasscodeComponent {
    fun appContext(): Context
    fun preferencesRepository(): PreferencesRepositoryAbs
    fun keyStoreManager(): KeyStoreManagerAbs
    fun passcodeTools(): PasscodeToolsAbs
    fun biometricTools(): BiometricToolsAbs
    fun biometricPrompt(): BiometricPromptAbs
    fun viewModelsFactory() : ViewModelsFactory

    fun inject(fragment: PasscodeSetupFragment)
    fun inject(activity: LockableActivity)
    fun inject(fragment: PasscodeEditFragment)
}

@Scope
annotation class PasscodeScope