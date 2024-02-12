/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.tools

import android.util.Base64
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_ACCESS_TOKEN
import com.saltedge.authenticator.core.api.model.EncryptedBundle
import com.saltedge.authenticator.core.tools.json.createDefaultGson
import com.saltedge.authenticator.core.tools.secure.BaseCryptoTools
import com.saltedge.authenticator.core.tools.secure.BaseCryptoToolsAbs
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationResponseData
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationV2Data
import timber.log.Timber
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom

object CryptoToolsV2 : BaseCryptoTools(), CryptoToolsV2Abs {

    override fun createEncryptedBundle(payload: String, rsaPublicKey: PublicKey?): EncryptedBundle? {
        return try {
            val key = ByteArray(32).also { SecureRandom().nextBytes(it) }
            val iv = ByteArray(16).also { SecureRandom().nextBytes(it) }
            val encryptedBytes = aesEncrypt(data = payload, key = key, iv = iv)
            return EncryptedBundle(
                encryptedAesKey = rsaEncrypt(inputBytes = key, publicKey = rsaPublicKey!!) ?: return null,
                encryptedAesIv = rsaEncrypt(inputBytes = iv, publicKey = rsaPublicKey) ?: return null,
                encryptedData = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
            )
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    override fun decryptAuthorizationData(
        encryptedData: AuthorizationResponseData,
        rsaPrivateKey: PrivateKey?
    ): AuthorizationV2Data? {
        return try {
            val privateKey = rsaPrivateKey ?: return null
            val encryptedKey = encryptedData.key
            val encryptedIV = encryptedData.iv
            val encryptedMessage = encryptedData.data
            val key = rsaDecrypt(encryptedKey, privateKey) ?: return null
            val iv = rsaDecrypt(encryptedIV, privateKey) ?: return null
            val jsonString = aesDecrypt(encryptedMessage, key = key, iv = iv)
            val result: AuthorizationV2Data = createDefaultGson().fromJson(jsonString, AuthorizationV2Data::class.java)
            result.connectionID = encryptedData.connectionId
            result.authorizationID = encryptedData.id
            result.status = encryptedData.status
            return result
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    fun decryptAccessToken(encryptedData: String, rsaPrivateKey: PrivateKey?): String? {
        return try {
            val privateKey = rsaPrivateKey ?: return null
            val encryptedBytes = Base64.decode(encryptedData.replace("\n", ""), Base64.NO_WRAP)
            val jsonString = String(rsaDecrypt(encryptedBytes, privateKey) ?: return null)
            val result = createDefaultGson().fromJson(jsonString, WrappedAccessToken::class.java)
            result.accessToken
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e(e)
            null
        }
    }
}

interface CryptoToolsV2Abs : BaseCryptoToolsAbs {
    fun createEncryptedBundle(payload: String, rsaPublicKey: PublicKey?): EncryptedBundle?
    fun decryptAuthorizationData(encryptedData: AuthorizationResponseData, rsaPrivateKey: PrivateKey?): AuthorizationV2Data?
}

data class WrappedAccessToken(@SerializedName(KEY_ACCESS_TOKEN) var accessToken: String)
