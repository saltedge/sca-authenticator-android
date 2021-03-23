/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.tools.crypt

import java.security.Key
import java.security.PrivateKey
import java.security.PublicKey

/**
 * Abstraction of CryptoTools
 * @see CryptoTools
 */
interface CryptoToolsAbs {
    fun rsaEncrypt(input: String, publicKey: PublicKey): String?
    fun rsaDecrypt(encryptedText: String, privateKey: PrivateKey): ByteArray?
    fun aesEncrypt(input: String, key: Key): String?
    fun aesDecrypt(encryptedText: String, key: Key): String?
    fun aesDecrypt(encryptedText: String, key: ByteArray, iv: ByteArray): String?
}