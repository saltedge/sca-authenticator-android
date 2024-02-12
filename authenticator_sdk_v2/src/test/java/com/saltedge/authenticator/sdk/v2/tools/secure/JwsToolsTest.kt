/*
 * Copyright (c) 2021 Salt Edge Inc.
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
