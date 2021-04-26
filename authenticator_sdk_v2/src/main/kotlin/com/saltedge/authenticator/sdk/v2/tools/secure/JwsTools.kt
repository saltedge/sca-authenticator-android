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
package com.saltedge.authenticator.sdk.v2.tools.secure

import com.saltedge.authenticator.sdk.v2.api.KEY_DATA
import com.saltedge.authenticator.sdk.v2.tools.json.createDefaultGson
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.gson.io.GsonSerializer
import java.security.Key
import java.util.*

object JwsTools {

    fun createSignature(requestDataObject: Any, expiresAt: Int, key: Key): String {
        val jws: String = Jwts.builder()
            .serializeToJsonWith(GsonSerializer(createDefaultGson()))
            .claim(KEY_DATA, requestDataObject)
            .signWith(key)
            .setExpiration(Date(expiresAt * 1000L))
            .compact()
        val sections: MutableList<String> = jws.split(".").toMutableList()
        sections[1] = ""
        return sections.joinToString(".")
    }
}
