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
package com.saltedge.authenticator.sdk.tools.biometric

import android.Manifest.permission.USE_FINGERPRINT
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import com.saltedge.authenticator.core.tools.biometric.BiometricTools
import com.saltedge.authenticator.core.tools.biometric.FINGERPRINT_ALIAS_FOR_PIN
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.sdk.testTools.TestTools
import io.mockk.every
import io.mockk.mockkClass
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BiometricToolsTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        assertThat(FINGERPRINT_ALIAS_FOR_PIN, equalTo("fingerprint_alias_for_pin"))
    }

    /**
     * Test isFingerprintAuthAvailable when isHardwareDetected and hasEnrolledFingerprints are false
     */
    @Test
    @Throws(Exception::class)
    fun isFingerprintAuthAvailableTestCase1() {
        every { mockContext.checkPermission(USE_FINGERPRINT, any(), any()) } returns PackageManager.PERMISSION_GRANTED
        every { mockFingerprintManager?.isHardwareDetected } returns false
        every { mockFingerprintManager?.hasEnrolledFingerprints() } returns false

        assertFalse(biometricTools.isFingerprintAuthAvailable(mockContext))
    }

    /**
     * Test isFingerprintAuthAvailable when isHardwareDetected is true and hasEnrolledFingerprints is false
     */
    @Test
    @Throws(Exception::class)
    fun isFingerprintAuthAvailableTestCase2() {
        every { mockContext.checkPermission(USE_FINGERPRINT, any(), any()) } returns PackageManager.PERMISSION_GRANTED
        every { mockFingerprintManager?.isHardwareDetected } returns true
        every { mockFingerprintManager?.hasEnrolledFingerprints() } returns false

        assertFalse(biometricTools.isFingerprintAuthAvailable(mockContext))
    }

    /**
     * Test isFingerprintAuthAvailable when isHardwareDetected is false and hasEnrolledFingerprints is true
     */
    @Test
    @Throws(Exception::class)
    fun isFingerprintAuthAvailableTestCase3() {
        every { mockContext.checkPermission(USE_FINGERPRINT, any(), any()) } returns PackageManager.PERMISSION_GRANTED
        every { mockFingerprintManager?.isHardwareDetected } returns false
        every { mockFingerprintManager?.hasEnrolledFingerprints() } returns true

        assertFalse(biometricTools.isFingerprintAuthAvailable(mockContext))
    }

    /**
     * Test isFingerprintAuthAvailable when isHardwareDetected hasEnrolledFingerprints are true, but permission denied
     */
    @Test
    @Throws(Exception::class)
    fun isFingerprintAuthAvailableTestCase5() {
        every { mockContext.checkPermission(USE_FINGERPRINT, any(), any()) } returns PackageManager.PERMISSION_DENIED
        every { mockFingerprintManager?.isHardwareDetected } returns true
        every { mockFingerprintManager?.hasEnrolledFingerprints() } returns true

        assertFalse(biometricTools.isFingerprintAuthAvailable(mockContext))
    }

    /**
     * Test isFingerprintAuthAvailable when isHardwareDetected hasEnrolledFingerprints are true
     */
    @Test
    @Throws(Exception::class)
    fun isFingerprintAuthAvailableTestCase4() {
        every { mockContext.checkPermission(USE_FINGERPRINT, any(), any()) } returns PackageManager.PERMISSION_GRANTED
        every { mockFingerprintManager?.isHardwareDetected } returns true
        every { mockFingerprintManager?.hasEnrolledFingerprints() } returns true

        assertTrue(biometricTools.isFingerprintAuthAvailable(mockContext))
    }

    @Test
    @Throws(Exception::class)
    fun activateFingerprintTest() {
        every { mockKeyStoreManager.keyEntryExist(FINGERPRINT_ALIAS_FOR_PIN) } returns false
        every { mockKeyStoreManager.createOrReplaceAesBiometricKey(FINGERPRINT_ALIAS_FOR_PIN) } returns null

        val biometricIsAvailable = biometricTools.isFingerprintAuthAvailable(TestTools.applicationContext)

        assertThat(biometricTools.activateFingerprint(), equalTo(biometricIsAvailable))
        assertThat(mockKeyStoreManager.keyEntryExist(FINGERPRINT_ALIAS_FOR_PIN), equalTo(biometricIsAvailable))
    }

    @Before
    @Throws(Exception::class)
    fun setUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mockFingerprintManager = mockkClass(FingerprintManager::class)
            every { mockContext.getSystemService(Context.FINGERPRINT_SERVICE) } returns mockFingerprintManager
        }
    }

    private val mockContext: Context = mockkClass(Context::class)
    private var mockFingerprintManager: FingerprintManager? = null
    private val mockKeyStoreManager = mockkClass(KeyManagerAbs::class)
    private val biometricTools = BiometricTools(mockContext, mockKeyStoreManager)
}
