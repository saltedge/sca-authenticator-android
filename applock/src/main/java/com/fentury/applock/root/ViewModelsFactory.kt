/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.root

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fentury.applock.passcode.edit.PasscodeEditViewModel
import com.fentury.applock.passcode.setup.PasscodeSetupViewModel
import com.fentury.applock.repository.PreferencesRepositoryAbs
import com.fentury.applock.tools.PasscodeToolsAbs
import com.fentury.applock.tools.biometric.BiometricToolsAbs
import com.fentury.applock.tools.crypt.CryptoToolsAbs
import com.fentury.applock.tools.keystore.KeyStoreManagerAbs
import javax.inject.Inject

class ViewModelsFactory @Inject constructor(
    val appContext: Context,
    val passcodeTools: PasscodeToolsAbs,
    val cryptoTools: CryptoToolsAbs,
    val keyStoreManager: KeyStoreManagerAbs,
    val preferencesRepository: PreferencesRepositoryAbs,
    val biometricTools: BiometricToolsAbs,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(PasscodeSetupViewModel::class.java) -> {
                return PasscodeSetupViewModel(
                    appContext = appContext,
                    passcodeTools = passcodeTools,
                    preferencesRepository = preferencesRepository,
                    biometricTools = biometricTools
                ) as T
            }
            modelClass.isAssignableFrom(PasscodeEditViewModel::class.java) -> {
                return PasscodeEditViewModel(passcodeTools) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}