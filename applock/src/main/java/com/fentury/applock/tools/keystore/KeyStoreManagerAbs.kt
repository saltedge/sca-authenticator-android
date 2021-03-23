/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.tools.keystore

import android.annotation.SuppressLint
import android.content.Context
import java.security.Key
import java.security.KeyPair
import javax.crypto.SecretKey

interface KeyStoreManagerAbs {
    fun createOrReplaceRsaKeyPair(context: Context?, alias: String): KeyPair?
    fun createRsaPublicKeyAsString(context: Context?, alias: String): String?
    fun keyEntryExist(alias: String): Boolean
    fun getKeyStoreAliases(): List<String>
    fun getKeyPair(alias: String?): KeyPair?
    fun deleteKeyPairs(guids: List<String>)
    fun deleteKeyPair(alias: String)
    fun getSecretKey(alias: String?): Key?
    @SuppressLint("NewApi") fun createOrReplaceAesBiometricKey(alias: String): SecretKey?
}