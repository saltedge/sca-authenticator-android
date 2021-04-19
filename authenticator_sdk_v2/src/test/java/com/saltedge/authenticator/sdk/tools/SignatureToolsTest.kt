/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
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
package com.saltedge.authenticator.sdk.tools

import android.util.Base64
import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.authenticator.sdk.network.connector.createExpiresAtTime
import com.saltedge.authenticator.sdk.tools.sign.createSignatureHeader
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
