/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.model

import com.saltedge.authenticator.core.api.model.DescriptionData
import com.saltedge.authenticator.sdk.v2.TestTools
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationV2Data
import com.saltedge.authenticator.sdk.v2.api.model.authorization.isNotExpired
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
        val authData = AuthorizationV2Data(
            title = "title",
            description = DescriptionData(),
            expiresAt = DateTime().withZone(DateTimeZone.UTC),
            authorizationCode = "111"
        )

        Assert.assertTrue(authData.copy(expiresAt = DateTime.now().plusMinutes(1)).isNotExpired())
        Assert.assertFalse(authData.copy(expiresAt = DateTime.now().minusMinutes(1)).isNotExpired())
    }
}
