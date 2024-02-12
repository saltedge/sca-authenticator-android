/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.tools.biometric

import com.saltedge.authenticator.core.tools.biometric.BiometricState
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class BiometricStateTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        val arrayOfFingerprintState = arrayOf(
            BiometricState.NOT_SUPPORTED,
            BiometricState.NOT_BLOCKED_DEVICE, BiometricState.NO_FINGERPRINTS,
            BiometricState.READY
        )
        assertThat(
            BiometricState.values(), equalTo(arrayOfFingerprintState)
        )
    }

    @Test
    @Throws(Exception::class)
    fun valueOfTest() {
        assertThat(
            BiometricState.valueOf("NOT_SUPPORTED"),
            equalTo(BiometricState.NOT_SUPPORTED)
        )
        assertThat(
            BiometricState.valueOf("NOT_BLOCKED_DEVICE"),
            equalTo(BiometricState.NOT_BLOCKED_DEVICE)
        )
        assertThat(
            BiometricState.valueOf("NO_FINGERPRINTS"),
            equalTo(BiometricState.NO_FINGERPRINTS)
        )
        assertThat(BiometricState.valueOf("READY"), equalTo(BiometricState.READY))
    }
}
