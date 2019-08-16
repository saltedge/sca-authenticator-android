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
package com.saltedge.authenticator.unitTests.tool.secure.fingerprint

import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.saltedge.authenticator.model.repository.PreferenceRepository
import com.saltedge.authenticator.sdk.tools.KeyStoreManager
import com.saltedge.authenticator.sdk.tools.publicKeyToPemEncodedString
import com.saltedge.authenticator.tool.secure.fingerprint.BiometricTools
import com.saltedge.authenticator.tool.secure.fingerprint.BiometricTools.isFingerprintAuthAvailable
import com.saltedge.authenticator.tool.secure.fingerprint.FINGERPRINT_ALIAS_FOR_PIN
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.M)
class BiometricToolsTest {

    private val mockContext: Context = mock(Context::class.java)
    private var mockFingerprintManager: FingerprintManager? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mockFingerprintManager = mock(FingerprintManager::class.java)
            Mockito.`when`(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(
                mockFingerprintManager
            )
        }
    }

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
        Mockito.`when`(mockFingerprintManager?.isHardwareDetected).thenReturn(false)
        Mockito.`when`(mockFingerprintManager?.hasEnrolledFingerprints()).thenReturn(false)

        assertFalse(isFingerprintAuthAvailable(mockContext))
    }

    /**
     * Test isFingerprintAuthAvailable when isHardwareDetected is true and hasEnrolledFingerprints is false
     */
    @Test
    @Throws(Exception::class)
    fun isFingerprintAuthAvailableTestCase2() {
        Mockito.`when`(mockFingerprintManager?.isHardwareDetected).thenReturn(true)
        Mockito.`when`(mockFingerprintManager?.hasEnrolledFingerprints()).thenReturn(false)

        assertFalse(isFingerprintAuthAvailable(mockContext))
    }

    /**
     * Test isFingerprintAuthAvailable when isHardwareDetected is false and hasEnrolledFingerprints is true
     */
    @Test
    @Throws(Exception::class)
    fun isFingerprintAuthAvailableTestCase3() {
        Mockito.`when`(mockFingerprintManager?.isHardwareDetected).thenReturn(false)
        Mockito.`when`(mockFingerprintManager?.hasEnrolledFingerprints()).thenReturn(true)

        assertFalse(isFingerprintAuthAvailable(mockContext))
    }

    /**
     * Test isFingerprintAuthAvailable when isHardwareDetected hasEnrolledFingerprints are true
     */
    @Test
    @Throws(Exception::class)
    fun isFingerprintAuthAvailableTestCase4() {
        Mockito.`when`(mockFingerprintManager?.isHardwareDetected).thenReturn(true)
        Mockito.`when`(mockFingerprintManager?.hasEnrolledFingerprints()).thenReturn(true)

        assertTrue(isFingerprintAuthAvailable(mockContext))
    }

    @Test
    @Throws(Exception::class)
    fun replaceFingerprintKeyTest() {
        if (KeyStoreManager.keyEntryExist(FINGERPRINT_ALIAS_FOR_PIN)) KeyStoreManager.deleteKeyPair(
            FINGERPRINT_ALIAS_FOR_PIN
        )

        assertFalse(KeyStoreManager.keyEntryExist(FINGERPRINT_ALIAS_FOR_PIN))

        val initialKey = BiometricTools.replaceFingerprintKey()!!

        assertTrue(KeyStoreManager.keyEntryExist(FINGERPRINT_ALIAS_FOR_PIN))

        val secondKey = BiometricTools.replaceFingerprintKey()!!

        assertTrue(KeyStoreManager.keyEntryExist(FINGERPRINT_ALIAS_FOR_PIN))
        assertThat(
            initialKey.publicKeyToPemEncodedString(),
            not(equalTo(secondKey.publicKeyToPemEncodedString()))
        )
    }

    @Test
    @Throws(Exception::class)
    fun activateFingerprintTest() {
        if (KeyStoreManager.keyEntryExist(FINGERPRINT_ALIAS_FOR_PIN)) {
            KeyStoreManager.deleteKeyPair(FINGERPRINT_ALIAS_FOR_PIN)
        }
        PreferenceRepository.fingerprintEnabled = false

        assertFalse(KeyStoreManager.keyEntryExist(FINGERPRINT_ALIAS_FOR_PIN))
        assertTrue(BiometricTools.activateFingerprint())
        assertTrue(KeyStoreManager.keyEntryExist(FINGERPRINT_ALIAS_FOR_PIN))
        assertTrue(PreferenceRepository.fingerprintEnabled)
    }
}
