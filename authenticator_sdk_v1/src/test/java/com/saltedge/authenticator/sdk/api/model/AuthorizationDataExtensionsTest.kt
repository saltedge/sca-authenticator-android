/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.model

import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationData
import com.saltedge.authenticator.sdk.api.model.authorization.isNotExpired
import com.saltedge.authenticator.sdk.testTools.TestTools
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthorizationDataExtensionsTest {

    @Before
    fun setUp() {
        JodaTimeAndroid.init(TestTools.applicationContext)
    }

    @Test
    @Throws(Exception::class)
    fun isNotExpiredTest() {
        val authData = AuthorizationData(
            id = "444",
            title = "title",
            description = "description",
            connectionId = "333",
            expiresAt = DateTime().withZone(DateTimeZone.UTC),
            authorizationCode = "111"
        )

        Assert.assertTrue(authData.copy(expiresAt = DateTime.now().plusMinutes(1)).isNotExpired())
        Assert.assertFalse(authData.copy(expiresAt = DateTime.now().minusMinutes(1)).isNotExpired())
    }
}
