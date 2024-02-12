/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.tools

import com.saltedge.authenticator.core.tools.hasHTMLTags
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HTMLToolsTest {

    @Test
    @Throws(Exception::class)
    fun hasHTMLTagsTest() {
        assertTrue("<a href='https://www.fentury.com/'>Fentury</a>".hasHTMLTags())
        assertFalse("Fentury.com".hasHTMLTags())
    }
}
