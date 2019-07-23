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
package com.saltedge.authenticator.sdk.tools

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.*
import java.security.spec.AlgorithmParameterSpec
import java.util.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

private const val STORE_TYPE = "AndroidKeyStore"
private const val KEY_ALGORITHM_RSA = "RSA"
private const val KEY_SIZE = 2048

object KeyStoreManager : KeyStoreManagerAbs {

    private var androidKeyStore: KeyStore? = null

    init {
        loadKeyStore()
    }

    /**
     * Creates new or replace existing RSA key pairs with new one by the given alias
     *
     * @param alias - the alias name
     * @return KeyPair object
     */
    override fun createOrReplaceRsaKeyPair(alias: String): KeyPair? {
        val store = androidKeyStore ?: return null
        if (store.containsAlias(alias)) deleteKeyPair(alias)
        return createNewRsaKeyPair(alias)
    }

    /**
     * Checks if key pair is exist by the given alias
     *
     * @param alias - the alias name
     * @return boolean, true if key store contains alias
     */
    override fun keyEntryExist(alias: String): Boolean {
        return androidKeyStore?.containsAlias(alias) ?: false
    }

    /**
     * Get key store aliases
     *
     * @return list of aliases
     */
    override fun getKeyStoreAliases(): List<String> {
        val aliases: Enumeration<String> = androidKeyStore?.aliases() ?: return emptyList()
        val result = ArrayList<String>()
        while (aliases.hasMoreElements()) result.add(aliases.nextElement())
        return result
    }

    /**
     * Get AES secret key by the given alias
     *
     * @return SecretKey object
     */
    fun getSecretKey(alias: String?): Key? {
        return androidKeyStore?.getKey(alias ?: return null, null)
    }

    /**
     * Get RSA key pair by the given alias
     *
     * @param alias - the alias name
     * @return KeyPair object
     */
    override fun getKeyPair(alias: String?): KeyPair? {
        val keyAlias = alias ?: return null
        val store = androidKeyStore ?: return null
        return (store.getKey(keyAlias, null) as? PrivateKey)?.let { privateKey ->
            val publicKey: PublicKey? = store.getCertificate(keyAlias).publicKey
            KeyPair(publicKey, privateKey)
        }
    }

    /**
     * Delete key pairs identified by list of guids
     *
     * @param guids - list of guids
     * @return list of guids
     */
    override fun deleteKeyPairs(guids: List<String>) = guids.forEach { deleteKeyPair(it) }

    /**
     * Delete key pair identified by the given alias from Android Keystore
     *
     * @param alias - the alias name
     */
    override fun deleteKeyPair(alias: String) {
        androidKeyStore?.let { if (it.containsAlias(alias)) it.deleteEntry(alias) }
    }

    /**
     * Creates or replace a AES (AES/GCM/NoPadding) secret key
     *
     * @param alias - the alias name
     * @return AES SecretKey object
     */
    fun createOrReplaceAesKey(alias: String): SecretKey? {
        return try {
            val mKeyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    STORE_TYPE
            )
            mKeyGenerator.init(KeyGenParameterSpec.Builder(alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(false)
                    .build())
            mKeyGenerator.generateKey()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Creates or replace a AES secret key, designated for biometric tools
     *
     * @param alias - the alias name
     * @return AES SecretKey object
     */
    override fun createOrReplaceAesBiometricKey(alias: String): SecretKey? {
        return try {
            val builder = KeyGenParameterSpec.Builder(alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(true)
            }
            val mKeyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    STORE_TYPE
            )
            mKeyGenerator.init(builder.build())
            mKeyGenerator.generateKey()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Create new RSA key pair by the given alias
     *
     * @param alias - the alias name
     * @return KeyPair object
     */
    fun createNewRsaKeyPair(alias: String): KeyPair? {
        val spec = getNewSdkKeyGenSpec(alias)
        return KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, STORE_TYPE)
                .apply { initialize(spec) }.generateKeyPair()
    }

    /**
     * Return algorithm parameter spec
     * Designated for Android version greater than or equal to SDK23
     *
     * @param alias - the alias name
     * @return algorithm parameter spec
     */
    private fun getNewSdkKeyGenSpec(alias: String): AlgorithmParameterSpec {
        return KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_SIGN)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setKeySize(KEY_SIZE)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .build()
    }

    /**
     * Load/Init KeyStore
     */
    private fun loadKeyStore() {
        try {
            androidKeyStore = KeyStore.getInstance(STORE_TYPE)
            androidKeyStore?.load(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * Convert public key from asymmetric key pair to pem string
 *
 * @receiver KeyPair object
 * @return public key as String
 */
fun KeyPair.publicKeyToPemEncodedString(): String {
    val encodedKey = encodeToPemBase64String(this.public.encoded)
    return "-----BEGIN PUBLIC KEY-----\n$encodedKey\n-----END PUBLIC KEY-----\n"
}
