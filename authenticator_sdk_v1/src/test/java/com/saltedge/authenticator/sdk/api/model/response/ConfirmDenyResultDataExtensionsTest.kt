/*
 * Copyright (c) 2019 Salt Edge Inc.
 */

package com.saltedge.authenticator.sdk.api.model.response

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfirmDenyResultDataExtensionsTest {

    @Test
    @Throws(Exception::class)
    fun isValidTest() {
        val model = ConfirmDenyResponseData(success = null, authorizationID = "")

        assertFalse(model.isValid())

        model.success = true
        model.authorizationID = ""

        assertFalse(model.isValid())

        model.success = null
        model.authorizationID = "authorizationId"

        assertFalse(model.isValid())

        model.success = true
        model.authorizationID = "authorizationId"

        assertTrue(model.isValid())
    }
}
