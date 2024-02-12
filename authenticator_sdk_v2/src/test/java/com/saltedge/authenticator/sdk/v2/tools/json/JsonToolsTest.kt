/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.tools.json

import com.saltedge.authenticator.core.api.model.DescriptionData
import com.saltedge.authenticator.core.tools.json.createDefaultGson
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationV2Data
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonToolsTest {

    @Test
    @Throws(Exception::class)
    fun createDefaultGsonTest() {
        val gson = createDefaultGson()
        val data = AuthorizationV2Data(
            title = "",
            description = DescriptionData(),
            authorizationCode = "Qwerty1+==",
            expiresAt = DateTime(0).withZone(DateTimeZone.UTC)
        )
        assertTrue(gson.toJson(data).contains("Qwerty1+=="))
        assertTrue(gson.toJson(data).contains("1970-01-01T00:00:00.000Z"))
    }
}
