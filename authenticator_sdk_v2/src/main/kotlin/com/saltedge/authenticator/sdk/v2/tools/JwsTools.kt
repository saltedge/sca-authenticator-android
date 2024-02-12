/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.tools

import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.tools.json.createDefaultGson
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
