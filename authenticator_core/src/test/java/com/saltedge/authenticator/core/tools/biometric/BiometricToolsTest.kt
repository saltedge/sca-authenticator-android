/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.tools.biometric

import android.Manifest.permission.USE_FINGERPRINT
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
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

    private val applicationContext: Context
        get() = ApplicationProvider.getApplicationContext<Context>().applicationContext

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

        val biometricIsAvailable = biometricTools.isFingerprintAuthAvailable(applicationContext)

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
