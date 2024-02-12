/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.tools

import android.util.Base64

fun decodeFromPemBase64String(inputString: String): ByteArray? {
    return Base64.decode(inputString.replace("\n", ""), Base64.NO_WRAP)
}

fun encodeToPemBase64String(inputArray: ByteArray): String? {
    val encodedString = Base64.encodeToString(inputArray, Base64.NO_WRAP) ?: return null
    return encodedString.splitToLines(64)
}
