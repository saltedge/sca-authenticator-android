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
@file:Suppress("DEPRECATION")

package com.saltedge.authenticator.sdk.v2.tools.secure

import android.util.Base64
import com.saltedge.authenticator.sdk.v2.tools.encodeToPemBase64String
import java.security.KeyFactory
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.KeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

const val DEFAULT_KEY_SIZE = 2048

object KeyAlgorithm {
    const val RSA = "RSA"
    const val AES = "AES"
}

/**
 * Convert private key from asymmetric key pair to pem string
 *
 * @receiver KeyPair object
 * @return private key as String
 */
fun KeyPair.privateKeyToPem(): String {
    val encodedKey = encodeToPemBase64String(this.private.encoded)
    return "-----BEGIN PRIVATE KEY-----\n$encodedKey\n-----END PRIVATE KEY-----\n"
}

/**
 * Convert public key from asymmetric key pair to pem string
 *
 * @receiver KeyPair object
 * @return public key as String
 */
fun KeyPair.publicKeyToPem(): String = this.public.publicKeyToPem()

/**
 * Convert public key to pem string
 *
 * @receiver RSA PublicKey
 * @return public key as String
 */
fun PublicKey.publicKeyToPem(): String {
    val encodedKey = encodeToPemBase64String(this.encoded)
    return "-----BEGIN PUBLIC KEY-----\n$encodedKey\n-----END PUBLIC KEY-----\n"
}

/**
 * Converts string which contains private key in PEM format to PrivateKey object
 *
 * @receiver private key in PEM format
 * @return PrivateKey or null
 */
fun String.pemToPrivateKey(algorithm: String): PrivateKey? {
    return try {
        val keyContent = this
            .replace("\\n", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
        val keySpec: KeySpec = PKCS8EncodedKeySpec(Base64.decode(keyContent, Base64.NO_WRAP))
        KeyFactory.getInstance(algorithm).generatePrivate(keySpec)
    } catch (e: Exception) {
        //TODO log
        null
    }
}

/**
 * Converts string which contains public key in PEM format to PublicKey object
 *
 * @receiver public key in PEM format
 * @return PublicKey or null
 */
fun String.pemToPublicKey(algorithm: String): PublicKey? {
    return try {
        val keyContent = this
            .replace("\\n", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
        val keySpecX509: KeySpec = X509EncodedKeySpec(Base64.decode(keyContent, Base64.NO_WRAP))
        KeyFactory.getInstance(algorithm).generatePublic(keySpecX509)
    } catch (e: Exception) {
        //TODO log
        null
    }
}
