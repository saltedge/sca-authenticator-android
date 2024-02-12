/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.tools

import android.util.Base64
import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.authenticator.core.tools.createExpiresAtTime
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.nio.charset.StandardCharsets
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature

@RunWith(RobolectricTestRunner::class)
class SignatureToolsTest {

    @Test
    @Throws(Exception::class)
    fun createSignatureHeaderTest() {
        val expiresAt = createExpiresAtTime()
        val signatureHeader = createSignatureHeader(
            requestMethod = "POST",
            requestUrl = "localhost",
            expiresAt = expiresAt.toString(),
            requestBody = "{}",
            privateKey = privateKey
        )

        val signatureHeaderBytes = Base64.decode(signatureHeader, Base64.NO_WRAP)
        val testPayload = "post|localhost|$expiresAt|{}"
        val signature: Signature = Signature.getInstance("SHA256withRSA")
        signature.initVerify(publicKey)
        signature.update(testPayload.toByteArray(StandardCharsets.UTF_8))

        Assert.assertTrue(signature.verify(signatureHeaderBytes))
    }

    private var privateKey: PrivateKey = CommonTestTools.testPrivateKey
    private var publicKey: PublicKey = CommonTestTools.testPublicKey
}
