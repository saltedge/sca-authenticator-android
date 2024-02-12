/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.app

import com.saltedge.authenticator.TestAppTools
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthenticatorApplicationTest {

    @Test
    @Throws(Exception::class)
    fun useAppContextTest() {
        val context = TestAppTools.applicationContext
        assertThat(
            context.packageName,
            anyOf(equalTo("com.saltedge.authenticator"), equalTo("com.saltedge.authenticator.debug"))
        )
    }
}
