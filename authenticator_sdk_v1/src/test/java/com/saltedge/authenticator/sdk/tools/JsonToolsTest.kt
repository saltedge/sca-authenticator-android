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

import com.saltedge.authenticator.core.tools.json.createDefaultGson
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationData
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonToolsTest {

    @Test
    @Throws(Exception::class)
    fun createDefaultGsonTest() {
        val gson = createDefaultGson()
        val data = AuthorizationData(
            id = "",
            title = "",
            description = "",
            authorizationCode = "Qwerty1+==",
            connectionId = "",
            expiresAt = DateTime(0).withZone(DateTimeZone.UTC)
        )
        assertTrue(gson.toJson(data).contains("Qwerty1+=="))
        assertTrue(gson.toJson(data).contains("1970-01-01T00:00:00.000Z"))
    }
}
