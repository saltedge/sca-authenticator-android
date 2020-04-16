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
package com.saltedge.authenticator.sdk.model

import com.saltedge.authenticator.sdk.constants.API_VERSION
import com.saltedge.authenticator.sdk.model.configuration.ProviderConfigurationData
import com.saltedge.authenticator.sdk.model.configuration.isValid
import org.junit.Assert
import org.junit.Test

class ProviderDataExtensionsTest {

    @Test
    @Throws(Exception::class)
    fun isValidTest() {
        val model = ProviderConfigurationData(
            code = "",
            name = "",
            connectUrl = "",
            logoUrl = "",
            version = "",
            supportEmail = ""
        )

        Assert.assertFalse(model.isValid())

        model.code = "demobank"
        model.name = ""
        model.connectUrl = ""
        model.logoUrl = ""
        model.supportEmail = ""

        Assert.assertFalse(model.isValid())

        model.code = ""
        model.name = "Demobank"
        model.connectUrl = ""
        model.logoUrl = ""
        model.supportEmail = ""

        Assert.assertFalse(model.isValid())

        model.code = "demobank"
        model.name = "Demobank"
        model.connectUrl = ""
        model.logoUrl = ""
        model.supportEmail = ""

        Assert.assertFalse(model.isValid())

        model.code = "demobank"
        model.name = "Demobank"
        model.connectUrl = "http://example.com"
        model.logoUrl = ""
        model.version = ""
        model.supportEmail = ""

        Assert.assertFalse(model.isValid())

        model.code = ""
        model.name = ""
        model.connectUrl = "http://example.com"
        model.logoUrl = "requestUrl"

        Assert.assertFalse(model.isValid())

        model.code = "demobank"
        model.name = "Demobank"
        model.connectUrl = "http://localhost"
        model.logoUrl = "requestUrl"
        model.version = API_VERSION
        model.supportEmail = ""

        Assert.assertFalse(model.isValid())

        model.code = "demobank"
        model.name = "Demobank"
        model.connectUrl = "requestUrl"
        model.logoUrl = "requestUrl"
        model.version = API_VERSION
        model.supportEmail = ""

        Assert.assertTrue(model.isValid())
    }
}
