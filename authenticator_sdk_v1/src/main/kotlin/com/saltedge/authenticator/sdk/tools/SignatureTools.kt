/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.tools

import android.util.Base64
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.security.PrivateKey
import java.security.Signature
import java.util.*

fun createSignatureHeader(
    requestMethod: String,
    requestUrl: String,
    expiresAt: String,
    requestBody: String,
    privateKey: PrivateKey
): String {
    val payload = "${requestMethod.lowercase(Locale.US)}|$requestUrl|$expiresAt|$requestBody"
    return runCatching {
        return payload.toByteArray(StandardCharsets.UTF_8).signWith(privateKey)?.let {
            Base64.encodeToString(it, Base64.NO_WRAP)
        } ?: return ""
    }.onFailure {
        Timber.e(it)
    }.getOrDefault(defaultValue = "")
}

private fun ByteArray.signWith(privateKey: PrivateKey): ByteArray? {
    val signature: Signature = Signature.getInstance("SHA256withRSA").also {
        it.initSign(privateKey)
        it.update(this)
    }
    return signature.sign()
}
