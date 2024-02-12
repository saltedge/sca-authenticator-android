/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
@file:Suppress("DEPRECATION")

package com.saltedge.authenticator.core.tools.secure

import android.util.Base64
import com.saltedge.authenticator.core.tools.encodeToPemBase64String
import timber.log.Timber
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.KeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec

const val DEFAULT_KEY_SIZE = 2048
const val PKCS1PublicHeader = "-----BEGIN RSA PUBLIC KEY-----"
const val PKCS1PublicFooter = "-----END RSA PUBLIC KEY-----"
const val PKCS8PublicHeader = "-----BEGIN PUBLIC KEY-----"
const val PKCS8PublicFooter = "-----END PUBLIC KEY-----"

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
            .replace("\\r\\n", "")
            .replace("\\n", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
        val keySpec: KeySpec = PKCS8EncodedKeySpec(Base64.decode(keyContent, Base64.NO_WRAP))
        KeyFactory.getInstance(algorithm).generatePrivate(keySpec)
    } catch (e: Exception) {
        Timber.e(e)
        null
    }
}

/**
 * Converts string which contains public key in PKCS#1 or PKCS#8 PEM format to PublicKey object
 *
 * @receiver public key in PKCS#1 or PKCS#8 PEM format
 * @return PublicKey or null if invalid
 */
fun String.pemToPublicKey(algorithm: String): PublicKey? {
    return try {
        val isRSAPublicKey = this.contains(PKCS1PublicHeader)
        val cleanedKeyContent = this
            .replace("\\r\\n", "")
            .replace("\\n", "").let {
                if (isRSAPublicKey) {
                    it.replace(PKCS1PublicHeader, "").replace(PKCS1PublicFooter, "")
                } else {
                    it.replace(PKCS8PublicHeader, "").replace(PKCS8PublicFooter, "")
                }
            }

        val keyBytes = Base64.decode(cleanedKeyContent, Base64.NO_WRAP)

        val keySpec: KeySpec = if (isRSAPublicKey) {
            val modulus = BigInteger(1, keyBytes)
            val exponent = BigInteger.valueOf(65537)
            RSAPublicKeySpec(modulus, exponent)
        } else {
            X509EncodedKeySpec(keyBytes)
        }

        KeyFactory.getInstance(algorithm).generatePublic(keySpec)
    } catch (e: Exception) {
        Timber.e(e, "Invalid PEM key: $this")
        null
    }
}
