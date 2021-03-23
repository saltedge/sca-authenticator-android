/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.root.di

import android.content.Context
import android.os.Build
import com.fentury.applock.root.ViewModelsFactory
import com.fentury.applock.repository.PreferencesRepository
import com.fentury.applock.repository.PreferencesRepositoryAbs
import com.fentury.applock.tools.PasscodeTools
import com.fentury.applock.tools.PasscodeToolsAbs
import com.fentury.applock.tools.biometric.BiometricTools
import com.fentury.applock.tools.biometric.BiometricToolsAbs
import com.fentury.applock.tools.crypt.CryptoTools
import com.fentury.applock.tools.crypt.CryptoToolsAbs
import com.fentury.applock.tools.keystore.KeyStoreManager
import com.fentury.applock.tools.keystore.KeyStoreManagerAbs
import com.fentury.applock.widget.biometric.BiometricPromptAbs
import com.fentury.applock.widget.biometric.BiometricPromptManagerV28
import com.fentury.applock.widget.biometric.BiometricsInputDialog
import dagger.Module
import dagger.Provides

@Module
class PasscodeModule(context: Context) {

    private var _context: Context = context

    @Provides
    @PasscodeScope
    fun provideAppContext(): Context = _context

    @Provides
    @PasscodeScope
    fun provideViewModelsFactory(
        appContext: Context,
        passcodeTools: PasscodeToolsAbs,
        biometricTools: BiometricToolsAbs,
        cryptoTools: CryptoToolsAbs,
        preferences: PreferencesRepositoryAbs,
        keyStoreManager: KeyStoreManagerAbs
    ): ViewModelsFactory {
        return ViewModelsFactory(
            appContext = appContext,
            passcodeTools = passcodeTools,
            biometricTools = biometricTools,
            cryptoTools = cryptoTools,
            preferencesRepository = preferences,
            keyStoreManager = keyStoreManager,
        )
    }

    @Provides
    @PasscodeScope
    fun providePasscodeTools(): PasscodeToolsAbs = PasscodeTools

    @Provides
    @PasscodeScope
    fun provideKeyStoreManager(): KeyStoreManagerAbs = KeyStoreManager

    @Provides
    @PasscodeScope
    fun provideCryptoTools(): CryptoToolsAbs = CryptoTools

    @Provides
    @PasscodeScope
    fun provideBiometricTools(): BiometricToolsAbs = BiometricTools

    @Provides
    fun provideBiometricPrompt(biometricTools: BiometricToolsAbs): BiometricPromptAbs {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) BiometricPromptManagerV28()
        else BiometricsInputDialog(biometricTools)
    }

    @Provides
    @PasscodeScope
    fun providePreferenceRepository(): PreferencesRepositoryAbs = PreferencesRepository
}