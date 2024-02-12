/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.app.di

import android.os.Build
import com.saltedge.authenticator.TestAppTools
import com.saltedge.authenticator.core.tools.biometric.BiometricToolsAbs
import com.saltedge.authenticator.widget.biometric.BiometricPromptManagerV28
import com.saltedge.authenticator.widget.biometric.BiometricsInputDialog
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
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
        assertNotNull(module.provideCryptoToolsV1())
        assertNotNull(module.provideCryptoToolsV2())
        assertNotNull(module.provideAuthenticatorApiManagerV1())
        assertNotNull(module.provideAuthenticatorApiManagerV2())
        assertNotNull(module.provideConnectionsRepository())
        assertNotNull(module.providePreferenceRepository())
        assertNotNull(module.provideKeyStoreManager())
    }

    @Test
    @Throws(Exception::class)
    fun provideBiometricPromptTest() {
        val module = AppModule(TestAppTools.applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            assertNotNull(module.provideBiometricPrompt(mockBiometricTools) is BiometricPromptManagerV28)
        } else {
            assertNotNull(module.provideBiometricPrompt(mockBiometricTools) is BiometricsInputDialog)
        }
    }

    private val mockBiometricTools = Mockito.mock(BiometricToolsAbs::class.java)
}
