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

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import com.saltedge.authenticator.sdk.v2.api.model.connection.ConnectionV2Abs
import com.saltedge.authenticator.sdk.v2.api.model.connection.RichConnection
import java.math.BigInteger
import java.security.*
import java.util.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.interfaces.DHPublicKey
import javax.crypto.spec.DHParameterSpec
import javax.security.auth.x500.X500Principal

private const val ANDROID_KEYSTORE = "AndroidKeyStore"

object KeyManager : KeyManagerAbs {

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
    override fun createOrReplaceRsaKeyPair(context: Context, alias: String): KeyPair? {
        return androidKeyStore?.let {
            deleteKeyPairIfExist(alias = alias)
            return generateRsaKeyPair(context = context, alias = alias)
        }
    }

    /**
     * Creates new or replace existing DH key pairs with new one by the given alias
     *
     * @param alias - the alias name
     * @return KeyPair object
     */
    override fun createOrReplaceDhKeyPair(context: Context, alias: String, outDhPublicKey: PublicKey): KeyPair? {
        return androidKeyStore?.let {
            deleteKeyPairIfExist(alias = alias)
            return generateDhKeyPair(context = context, alias = alias, outDhPublicKey = outDhPublicKey)
        }
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
    override fun getSecretKey(alias: String): Key? {
        return androidKeyStore?.getKey(alias, null)
    }

    /**
     * Get RSA key pair by the given alias
     *
     * @param alias - the alias name
     * @return KeyPair object
     */
    override fun getKeyPair(alias: String): KeyPair? {
        val store = androidKeyStore ?: return null
        return (store.getKey(alias, null) as? PrivateKey)?.let { privateKey ->
            KeyPair(store.getCertificate(alias).publicKey, privateKey)
        }
    }

    /**
     * Delete key pairs identified by list of guids
     *
     * @param guids - list of guids
     * @return list of guids
     */
    override fun deleteKeyPairsIfExist(guids: List<String>) = guids.forEach { deleteKeyPairIfExist(it) }

    /**
     * Delete key pair identified by the given alias from Android Keystore
     *
     * @param alias - the alias name
     */
    override fun deleteKeyPairIfExist(alias: String) {
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
            e.printStackTrace()
            null
        }
    }

    /**
     *  Get related private key for connection
     *
     *  @param connection Connection
     *  @return ConnectionAndKey
     */
    override fun enrichConnection(connection: ConnectionV2Abs): RichConnection? {
        val rsaPrivate = getKeyPair(alias = connection.guid)?.private ?: return null
        val appDhKeyPair = getKeyPair(alias = connection.appDhKeyAlias) ?: return null
        val providerDhPublicKey = convertPemToPublicKey(
            pem = connection.providerDhPublicKey,
            algorithm = KeyAlgorithm.DIFFIE_HELLMAN
        ) ?: return null
        val aesSharedSecret = KeyTools.computeSecretKey(
            privateDhKey = appDhKeyPair.private,
            publicDhKey = providerDhPublicKey
        )
        return RichConnection(connection, rsaPrivate, aesSharedSecret)
    }

    /**
     * Create new RSA key pair by the given alias
     *
     * @param alias - the alias name
     * @return KeyPair object
     */
    private fun generateRsaKeyPair(context: Context, alias: String): KeyPair? {
        return (if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            initAsymmetricKeyGenerator(context = context, alias = alias, keyAlgorithm = KeyAlgorithm.RSA)
        } else {
            initRsaKeyGenerator(alias = alias)
        }).generateKeyPair()
    }

    /**
     * Create new Diffie-Hellman key pair by the given alias
     *
     * @param alias - the alias name
     * @return KeyPair object
     */
    private fun generateDhKeyPair(context: Context, alias: String, outDhPublicKey: PublicKey): KeyPair? {
        return (if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            initAsymmetricKeyGenerator(
                context = context,
                alias = alias,
                keyAlgorithm = KeyAlgorithm.DIFFIE_HELLMAN,
                outDhPublicKey = outDhPublicKey
            )
        } else {
            initDhKeyGenerator(alias = alias, outDhPublicKey = outDhPublicKey)
        })?.generateKeyPair()
    }

    /**
     * Initialize old type KeyPairGenerator
     * Designated for Android version less than SDK23
     *
     * @param context Application Context
     * @param alias the alias name
     * @return generator KeyPairGenerator
     */
    @Suppress("DEPRECATION")
    private fun initAsymmetricKeyGenerator(context: Context, alias: String, keyAlgorithm: String, outDhPublicKey: PublicKey? = null): KeyPairGenerator {
        val builder = KeyPairGeneratorSpec.Builder(context)
            .setAlias(alias)
            .setSerialNumber(BigInteger.ONE)
            .setSubject(X500Principal("CN=${alias} CA Certificate"))
            .setStartDate(Calendar.getInstance().time)
            .setEndDate((Calendar.getInstance().apply { add(Calendar.YEAR, 20) }).time)
        (outDhPublicKey as? DHPublicKey)?.params?.let { builder.setAlgorithmParameterSpec(it) }
        return  KeyPairGenerator.getInstance(keyAlgorithm, ANDROID_KEYSTORE).apply {
            initialize(builder.build())
        }
    }

    /**
     * Initialize KeyPairGenerator for RSA
     * Designated for Android version greater than or equal to SDK23
     *
     * @param alias the alias name
     * @return generator KeyPairGenerator
     */
    @TargetApi(Build.VERSION_CODES.M)
    private fun initRsaKeyGenerator(alias: String): KeyPairGenerator {
        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT
                or KeyProperties.PURPOSE_DECRYPT
                or KeyProperties.PURPOSE_SIGN
        )
        builder.setDigests(KeyProperties.DIGEST_SHA256)
        builder.setKeySize(DEFAULT_KEY_SIZE)
        builder.setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
        builder.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
        return  KeyPairGenerator.getInstance(KeyAlgorithm.RSA, ANDROID_KEYSTORE).apply {
            initialize(builder.build())
        }
    }

    /**
     * Create new Diffie-Hellman key pair by the given alias
     *
     * @param alias - the alias name
     * @return KeyPair object
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initDhKeyGenerator(alias: String, outDhPublicKey: PublicKey): KeyPairGenerator? {
        val dhParams: DHParameterSpec = (outDhPublicKey as DHPublicKey).params

        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT
                or KeyProperties.PURPOSE_DECRYPT
                or KeyProperties.PURPOSE_SIGN
        )
        .setAlgorithmParameterSpec(dhParams)
        .setDigests(KeyProperties.DIGEST_SHA256)
        .setRandomizedEncryptionRequired(true)

        return  KeyPairGenerator.getInstance(KeyAlgorithm.DIFFIE_HELLMAN, ANDROID_KEYSTORE).apply {
            initialize(builder.build())
        }
    }

    /**
     * Load/Init KeyStore
     */
    private fun loadKeyStore() {
        try {
            androidKeyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            androidKeyStore?.load(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

interface KeyManagerAbs {
    fun createOrReplaceRsaKeyPair(context: Context, alias: String): KeyPair?
    fun createOrReplaceDhKeyPair(context: Context, alias: String, outDhPublicKey: PublicKey): KeyPair?
    fun keyEntryExist(alias: String): Boolean
    fun getKeyStoreAliases(): List<String>
    fun getSecretKey(alias: String): Key?
    fun getKeyPair(alias: String): KeyPair?
    fun deleteKeyPairsIfExist(guids: List<String>)
    fun deleteKeyPairIfExist(alias: String)
    @SuppressLint("NewApi") fun createOrReplaceAesBiometricKey(alias: String): SecretKey?
    fun enrichConnection(connection: ConnectionV2Abs): RichConnection?
}
