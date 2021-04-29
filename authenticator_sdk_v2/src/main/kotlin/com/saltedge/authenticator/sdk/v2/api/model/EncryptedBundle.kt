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
package com.saltedge.authenticator.sdk.v2.api.model

import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.sdk.v2.api.KEY_DATA
import com.saltedge.authenticator.sdk.v2.api.KEY_IV
import com.saltedge.authenticator.sdk.v2.api.KEY_KEY
import java.io.Serializable

/**
 * Encrypted data bundle. Based on hybrid crypto algorithm.
 *
 * Payload is encrypted with symmetric AES-CBC-256 algorithm.
 * AES secret key and initialization vector are encrypted with asymmetric RSA-256 algorithm.
 * All strings are encoded with Base64 algorithm.
 *
 * @param encryptedAesKey AES secret key encrypted with RSA algorithm and encoded with Base64 algorithm
 * @param encryptedIvKey AES initialization vector encrypted with RSA algorithm and encoded with Base64 algorithm
 * @param encryptedData AES secret key encrypted with RSA algorithm and encoded with Base64 algorithm
 */
data class EncryptedBundle(
    @SerializedName(KEY_KEY) var encryptedAesKey: String,
    @SerializedName(KEY_IV) var encryptedIvKey: String,
    @SerializedName(KEY_DATA) var encryptedData: String
) : Serializable
