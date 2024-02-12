/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.tools.json

import com.saltedge.authenticator.core.model.ActionAppLinkData
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonToolsTest {

    @Test
    @Throws(Exception::class)
    fun createDefaultGsonTest() {
        val gson = createDefaultGson()
        val data = ActionAppLinkData(
            apiVersion = "1",
            providerID = null,
            actionIdentifier = "Qwerty1+==",
            connectUrl = "",
            returnTo = ""
        )
        assertTrue(gson.toJson(data).contains("Qwerty1+=="))
    }
}
