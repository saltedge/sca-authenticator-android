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
@file:Suppress("DEPRECATION")

package com.saltedge.authenticator.sdk.tools.keystore

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import com.saltedge.authenticator.sdk.model.connection.ConnectionAbs
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.tools.encodeToPemBase64String
import timber.log.Timber
import java.math.BigInteger
import java.security.*
import java.util.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.security.auth.x500.X500Principal

private const val ANDROID_KEYSTORE = "AndroidKeyStore"
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
    override fun createOrReplaceRsaKeyPair(context: Context?, alias: String): KeyPair? {
        val store = androidKeyStore ?: return null
        if (store.containsAlias(alias)) deleteKeyPair(alias = alias)
        return createNewRsaKeyPair(context = context, alias = alias)
    }

    /**
     * Creates new or replace existing RSA key pairs with new one by the given alias,
     * convert public key from asymmetric key pair to pem string
     *
     * @param alias - the alias name
     * @return public key as String
     */
    override fun createRsaPublicKeyAsString(context: Context?, alias: String): String? {
        return createOrReplaceRsaKeyPair(context = context, alias = alias)?.publicKeyToPemEncodedString()
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
    override fun getSecretKey(alias: String?): Key? {
        return androidKeyStore?.getKey(alias ?: return null, null)
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
        return try {
            val mKeyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val purposes = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            val spec = KeyGenParameterSpec.Builder(alias, purposes)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(false)
                .build()
            mKeyGenerator.init(spec)
            mKeyGenerator.generateKey()
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    /**
     * Creates or replace a AES secret key, designated for biometric tools
     *
     * @param alias - the alias name
     * @return AES SecretKey object
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun createOrReplaceAesBiometricKey(alias: String): SecretKey? {
        return try {
            val builder = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
            builder.setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            builder.setUserAuthenticationRequired(true)
            builder.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(true)
            }
            val mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            mKeyGenerator.init(builder.build())
            mKeyGenerator.generateKey()
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    /**
     * Create new RSA key pair by the given alias
     *
     * @param alias - the alias name
     * @return KeyPair object
     */
    fun createNewRsaKeyPair(context: Context?, alias: String): KeyPair? {
        val generator = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            initGeneratorWithKeyPairGeneratorSpec(
                context = context ?: return null,
                generator = generator,
                alias = alias
            )
        } else {
            initGeneratorWithKeyGenParameterSpec(generator = generator, alias = alias)
        }
        return generator.generateKeyPair()
    }

    /**
     *  Get related private key for connection
     *
     *  @param connection Connection
     *  @return ConnectionAndKey
     */
    override fun createConnectionAndKeyModel(connection: ConnectionAbs): ConnectionAndKey? {
        return getKeyPair(connection.guid)?.private?.let { key ->
            ConnectionAndKey(connection, key)
        }
    }

    /**
     * Get RSA key pair by the given alias
     *
     * @param alias - the alias name
     * @return KeyPair object
     */
    override fun getKeyPair(alias: String?): KeyPair? {
        return try {
            val keyAlias = alias ?: return null
            val store = androidKeyStore ?: return null
            (store.getKey(keyAlias, null) as? PrivateKey)?.let { privateKey ->
                val publicKey: PublicKey? = store.getCertificate(keyAlias).publicKey
                KeyPair(publicKey, privateKey)
            }
        } catch (e: UnrecoverableKeyException) {
            androidKeyStore?.deleteEntry(alias)
            null
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    /**
     * Initialize KeyPairGenerator
     * Designated for Android version less than SDK23
     *
     * @param context Application Context
     * @param generator KeyPairGenerator
     * @param alias the alias name
     * @return algorithm parameter spec
     */
    @Suppress("DEPRECATION")
    private fun initGeneratorWithKeyPairGeneratorSpec(
        context: Context,
        generator: KeyPairGenerator,
        alias: String
    ) {
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.YEAR, 20)

        val builder = KeyPairGeneratorSpec.Builder(context)
            .setAlias(alias)
            .setSerialNumber(BigInteger.ONE)
            .setSubject(X500Principal("CN=${alias} CA Certificate"))
            .setStartDate(startDate.time)
            .setEndDate(endDate.time)
        generator.initialize(builder.build())
    }

    /**
     * Initialize KeyPairGenerator
     * Designated for Android version greater than or equal to SDK23
     *
     * @param generator KeyPairGenerator
     * @param alias the alias name
     * @return algorithm parameter spec
     */
    @TargetApi(Build.VERSION_CODES.M)
    private fun initGeneratorWithKeyGenParameterSpec(generator: KeyPairGenerator, alias: String) {
        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT
                or KeyProperties.PURPOSE_DECRYPT
                or KeyProperties.PURPOSE_SIGN
        )
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setKeySize(KEY_SIZE)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
        generator.initialize(builder.build())
    }

    /**
     * Load/Init KeyStore
     */
    private fun loadKeyStore() {
        try {
            androidKeyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            androidKeyStore?.load(null)
        } catch (e: Exception) {
            Timber.e(e)
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
