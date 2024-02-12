/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.tools

import com.saltedge.authenticator.core.api.model.EncryptedData
import com.saltedge.authenticator.core.tools.json.createDefaultGson
import com.saltedge.authenticator.core.tools.secure.BaseCryptoTools
import com.saltedge.authenticator.core.tools.secure.BaseCryptoToolsAbs
import com.saltedge.authenticator.core.tools.secure.SUPPORTED_AES_ALGORITHM
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationData
import java.security.PrivateKey

object CryptoToolsV1 : BaseCryptoTools(), CryptoToolsV1Abs {

    override fun decryptAuthorizationData(
        encryptedData: EncryptedData,
        rsaPrivateKey: PrivateKey?
    ): AuthorizationData? {
        val algorithm = encryptedData.algorithm
        if (algorithm != null && algorithm != SUPPORTED_AES_ALGORITHM) return null
        return try {
            val privateKey = rsaPrivateKey ?: return null
            val encryptedKey = encryptedData.key
            val encryptedIV = encryptedData.iv
            val encryptedMessage = encryptedData.data
            val key = rsaDecrypt(encryptedKey, privateKey) ?: return null
            val iv = rsaDecrypt(encryptedIV, privateKey) ?: return null
            val jsonString = aesDecrypt(encryptedMessage, key = key, iv = iv)
            createDefaultGson().fromJson(jsonString, AuthorizationData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

interface CryptoToolsV1Abs : BaseCryptoToolsAbs {
    fun decryptAuthorizationData(encryptedData: EncryptedData, rsaPrivateKey: PrivateKey?): AuthorizationData?
}
