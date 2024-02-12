/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.tools

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TextToolsTest {

    @Test
    @Throws(Exception::class)
    fun isNotNullOrEmptyTest() {
        Assert.assertFalse("".isPresent())
        Assert.assertTrue("authenticator".isPresent())
    }
}
