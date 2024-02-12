/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.unitTests.tool

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.instrumentationTestTools.TestTools
import com.saltedge.authenticator.app.AppTools
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppToolsTest {

    @Test
    @Throws(Exception::class)
    fun testIsTestsSuite() {
        Assert.assertTrue(
            TestTools.applicationContext.classLoader?.toString()?.contains("test") ?: false
        )
        Assert.assertTrue(AppTools.isTestsSuite(TestTools.applicationContext))
    }
}
