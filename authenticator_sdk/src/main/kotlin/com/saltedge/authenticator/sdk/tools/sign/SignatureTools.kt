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
package com.saltedge.authenticator.sdk.tools.sign

import android.util.Base64
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
    val payload = "${requestMethod.toLowerCase(Locale.US)}|$requestUrl|$expiresAt|$requestBody"
    return payload.toByteArray(StandardCharsets.UTF_8).signWith(privateKey)?.let {
        Base64.encodeToString(it, Base64.NO_WRAP)
    } ?: return ""
}

private fun ByteArray.signWith(privateKey: PrivateKey): ByteArray? {
    val signature: Signature = Signature.getInstance("SHA256withRSA")
    signature.initSign(privateKey)
    signature.update(this)
    return signature.sign()
}
