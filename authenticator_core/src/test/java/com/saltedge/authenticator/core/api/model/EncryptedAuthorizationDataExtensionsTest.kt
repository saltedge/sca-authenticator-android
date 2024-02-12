/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.api.model

import org.junit.Assert
import org.junit.Test

class EncryptedAuthorizationDataExtensionsTest {

    @Test
    @Throws(Exception::class)
    fun isValidTest() {
        Assert.assertFalse(
            EncryptedData(
                id = "1",
                key = "key",
                iv = "iv",
                algorithm = "",
                data = "data",
                connectionId = "333"
            ).isValid()
        )
        Assert.assertFalse(
            EncryptedData(
                id = "1",
                key = "key",
                iv = "iv",
                algorithm = "alg",
                data = "data",
                connectionId = ""
            ).isValid()
        )
        Assert.assertFalse(
            EncryptedData(
                id = "1",
                key = "key",
                iv = "iv",
                algorithm = "alg",
                data = "",
                connectionId = "333"
            ).isValid()
        )
        Assert.assertFalse(
            EncryptedData(
                id = "1",
                key = "key",
                iv = "",
                algorithm = "alg",
                data = "data",
                connectionId = "333"
            ).isValid()
        )
        Assert.assertFalse(
            EncryptedData(
                id = "1",
                key = "",
                iv = "iv",
                algorithm = "alg",
                data = "data",
                connectionId = "333"
            ).isValid()
        )
        Assert.assertTrue(
            EncryptedData(
                id = "",
                key = "key",
                iv = "iv",
                algorithm = "alg",
                data = "data",
                connectionId = "333"
            ).isValid()
        )
        Assert.assertTrue(
            EncryptedData(
                id = "1",
                key = "key",
                iv = "iv",
                algorithm = "alg",
                data = "data",
                connectionId = "333"
            ).isValid()
        )
    }
}
