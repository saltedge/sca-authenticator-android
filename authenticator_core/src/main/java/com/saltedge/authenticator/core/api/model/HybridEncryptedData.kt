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
package com.saltedge.authenticator.core.api.model

import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.*
import com.saltedge.authenticator.core.tools.isNotEmptyIfDeclared
import com.saltedge.authenticator.core.tools.isPresent
import java.io.Serializable

/**
 * Encrypted data bundle. Based on hybrid crypto algorithm.
 *
 * Payload is encrypted with symmetric AES-CBC-256 algorithm.
 * AES secret key and initialization vector are encrypted with asymmetric RSA-256 algorithm.
 * All strings are encoded with Base64 algorithm.
 *
 * @param encryptedAesKey AES secret key encrypted with RSA algorithm and encoded with Base64 algorithm
 * @param encryptedAesIv AES initialization vector encrypted with RSA algorithm and encoded with Base64 algorithm
 * @param encryptedData AES secret key encrypted with RSA algorithm and encoded with Base64 algorithm
 */
data class EncryptedBundle(
    @SerializedName(KEY_KEY) val encryptedAesKey: String,
    @SerializedName(KEY_IV) val encryptedAesIv: String,
    @SerializedName(KEY_DATA) val encryptedData: String
) : Serializable

/**
 * Encrypted model
 * with annotation for GSON parsing
 * contains:
 * encryption key (KEY), encrypted with asymmetric RSA key attached to specific connection (connectionId)
 * initialization vector (IV), encrypted with asymmetric RSA key attached to specific connection (connectionId)
 * encrypted data (any data) with symmetric algorithm,
 * algorithm code (now is supported only AES-CBC-256),
 */
data class EncryptedData(
    @SerializedName(KEY_ID) var id: String,
    @SerializedName(KEY_CONNECTION_ID) var connectionId: String,
    @SerializedName(KEY_KEY) var key: String,
    @SerializedName(KEY_IV) var iv: String,
    @SerializedName(KEY_DATA) var data: String,
    @SerializedName(KEY_ALGORITHM) var algorithm: String? = null,
) : Serializable

/**
 * Checks validity of Encrypted model
 *
 * @receiver Encrypted data
 * @return boolean. true if key, iv, algorithm, data, connection_id fields are present
 */
fun EncryptedData.isValid(): Boolean {
    return algorithm.isNotEmptyIfDeclared() &&
        key.isPresent() &&
        iv.isPresent() &&
        data.isPresent() &&
        connectionId.isPresent()
}
