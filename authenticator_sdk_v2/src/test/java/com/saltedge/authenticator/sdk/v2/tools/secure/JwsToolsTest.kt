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
package com.saltedge.authenticator.sdk.v2.tools.secure

import android.util.Base64
import com.google.gson.internal.LinkedTreeMap
import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.authenticator.core.api.model.EncryptedBundle
import com.saltedge.authenticator.core.tools.json.createDefaultGson
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationRequest
import com.saltedge.authenticator.sdk.v2.tools.JwsTools
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.security.PrivateKey
import java.security.PublicKey

@RunWith(RobolectricTestRunner::class)
class JwsToolsTest {

    @Test
    @Throws(Exception::class)
    fun createSignatureHeaderTest() {
        val request = UpdateAuthorizationRequest(EncryptedBundle("", "", ""))
        val jwsSignature: String = JwsTools.createSignature(
            requestDataObject = request.data,
            expiresAt = request.requestExpirationTime,
            key = privateKey
        )

        Assert.assertTrue(jwsSignature.contains(".."))

        val rawRequestBody = createDefaultGson().toJson(request)

        val jwsParts = jwsSignature.split(".").toMutableList()
        jwsParts[1] = Base64.encodeToString(rawRequestBody.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val encodedJws = jwsParts.joinToString(".")
        val claims: Jws<Claims> = Jwts.parserBuilder()
            .setSigningKey(publicKey)
            .build()
            .parseClaimsJws(encodedJws)
        val map = claims.body.get("data", LinkedTreeMap::class.java)

        assertThat(map.size, equalTo(3))
    }

    private var privateKey: PrivateKey = CommonTestTools.testPrivateKey
    private var publicKey: PublicKey = CommonTestTools.testPublicKey
}
