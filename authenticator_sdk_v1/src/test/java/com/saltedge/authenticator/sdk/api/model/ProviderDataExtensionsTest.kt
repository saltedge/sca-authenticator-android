/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.model

import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.api.model.configuration.ProviderConfigurationData
import com.saltedge.authenticator.sdk.api.model.configuration.isValid
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
            supportEmail = "",
            consentManagementSupported = true,
            geolocationRequired = true
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
        model.version = API_V1_VERSION
        model.supportEmail = ""

        Assert.assertFalse(model.isValid())

        model.code = "demobank"
        model.name = "Demobank"
        model.connectUrl = "requestUrl"
        model.logoUrl = "requestUrl"
        model.version = API_V1_VERSION
        model.supportEmail = ""

        Assert.assertTrue(model.isValid())
    }
}
