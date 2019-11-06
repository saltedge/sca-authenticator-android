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
package com.saltedge.authenticator.app.di

import com.saltedge.authenticator.sdk.tools.biometric.isBiometricPromptV28Enabled
import com.saltedge.authenticator.testTools.TestAppTools
import com.saltedge.authenticator.widget.biometric.BiometricPromptManagerV28
import com.saltedge.authenticator.widget.biometric.BiometricsInputDialog
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppModuleTest {

    @Test
    @Throws(Exception::class)
    fun modulesTest() {
        val module = AppModule(TestAppTools.applicationContext)

        assertNotNull(module.provideAppContext())
        assertNotNull(module.provideBiometricTools())
        assertNotNull(module.providePasscodeTools())
        assertNotNull(module.provideCryptoTools())
        assertNotNull(module.provideAuthenticatorApiManager())
        assertNotNull(module.provideConnectionsRepository())
        assertNotNull(module.providePreferenceRepository())
        assertNotNull(module.provideKeyStoreManager())
    }

    @Test
    @Throws(Exception::class)
    fun provideBiometricPromptTest() {
        val module = AppModule(TestAppTools.applicationContext)
        if (isBiometricPromptV28Enabled()) {
            assertNotNull(module.provideBiometricPrompt() is BiometricPromptManagerV28)
        } else {
            assertNotNull(module.provideBiometricPrompt() is BiometricsInputDialog)
        }
    }
}
