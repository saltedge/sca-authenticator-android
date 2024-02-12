/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.tools

import com.saltedge.authenticator.core.model.ActionAppLinkData
import com.saltedge.authenticator.core.model.ConnectAppLinkData
import com.saltedge.authenticator.core.model.extractActionAppLinkData
import com.saltedge.authenticator.core.model.extractConnectAppLinkData
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert
import org.junit.Assert.assertNull
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppLinkToolsTest {

    @Test
    @Throws(Exception::class)
    fun isValidDeeplinkTest() {
        Assert.assertFalse("".isValidAppLink())
        Assert.assertFalse("test".isValidAppLink())
        Assert.assertFalse("https://google.com".isValidAppLink())
        Assert.assertFalse("authenticator://saltedge.com/connect?configuration=https://localhost/configuration".isValidAppLink())
        Assert.assertTrue("authenticator://saltedge.com/connect?configuration=https://example.com/configuration".isValidAppLink())
        Assert.assertTrue("authenticator://saltedge.com/connect?configuration=https://example.com/configuration&connect_query=1234567890".isValidAppLink())
    }

    @Test
    @Throws(Exception::class)
    fun extractConnectAppLinkDataTest() {
        assertNull("".extractConnectAppLinkData())
        assertNull("test".extractConnectAppLinkData())
        assertNull("....".extractConnectAppLinkData())
        assertNull("////".extractConnectAppLinkData())
        assertNull("https://google.com".extractConnectAppLinkData())
        assertNull("authenticator://saltedge.com/connect?configuration=https://localhost/configuration".extractConnectAppLinkData())
        assertNull("authenticator://saltedge.com/connect?configuration=https://backend/api/authenticator/v1/configuration".extractConnectAppLinkData())
        assertThat(
            "authenticator://saltedge.com/connect?configuration=https://example.com/configuration".extractConnectAppLinkData(),
            equalTo(ConnectAppLinkData(configurationUrl = "https://example.com/configuration", connectQuery = null))
        )
        assertThat(
            "authenticator://saltedge.com/connect?configuration=https://example.com/configuration&connect_query=1234567890".extractConnectAppLinkData(),
            equalTo(ConnectAppLinkData(configurationUrl = "https://example.com/configuration", connectQuery = "1234567890"))
        )
    }

    @Test
    @Throws(Exception::class)
    fun extractActionExtractDeepLinkDataTest() {
        assertNull("".extractActionAppLinkData())
        assertNull("authenticator://saltedge.com/action?action=123456".extractActionAppLinkData())
        assertThat(
            ("authenticator://saltedge.com/action?action_uuid=123456" +
                "&connect_url=https://www.saltedge.com/").extractActionAppLinkData(),
            equalTo(
                ActionAppLinkData(
                    apiVersion = "1",
                    providerID = null,
                    actionIdentifier = "123456",
                    connectUrl = "https://www.saltedge.com/",
                    returnTo = null
                )
            )
        )
        assertThat(
            ("authenticator://saltedge.com/action?action_uuid=123456&return_to=https://www.saltedge.com/" +
                "&connect_url=http://www.fentury.com/").extractActionAppLinkData(),
            equalTo(
                ActionAppLinkData(
                    apiVersion = "1",
                    providerID = null,
                    actionIdentifier = "123456",
                    connectUrl = "http://www.fentury.com/",
                    returnTo = "https://www.saltedge.com/"
                )
            )
        )

        assertThat(
            ("authenticator://saltedge.com/action?api_version=2&action_id=1&provider_id=1&return_to=http://return.com").extractActionAppLinkData(),
            equalTo(
                ActionAppLinkData(
                    apiVersion = "2",
                    providerID = "1",
                    actionIdentifier = "1",
                    connectUrl = null,
                    returnTo = "http://return.com"
                )
            )
        )
    }
}
