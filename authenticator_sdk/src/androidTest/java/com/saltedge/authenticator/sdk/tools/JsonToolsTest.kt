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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.sdk.model.ProviderData
import com.saltedge.authenticator.sdk.testTools.toJsonString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JsonToolsTest {

    @Test
    @Throws(Exception::class)
    fun toJsonStringTest() {
        assertEquals("\"test\"", "test".toJsonString())

        val data = ProviderData(
            connectUrl = "",
            logoUrl = "",
            name = "",
            code = "",
            version = "",
            supportEmail = "example@example.com"
        )

        assertTrue(data.toJsonString().isNotEmpty())
    }
}
