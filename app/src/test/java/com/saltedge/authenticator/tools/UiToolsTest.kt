/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.tools

import android.graphics.Color
import com.saltedge.authenticator.R
import com.saltedge.authenticator.TestAppTools
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UiToolsTest {

    @Test
    @Throws(Exception::class)
    fun testApplyAlphaToColor() {
        assertEquals(Color.BLACK, Color.BLACK.applyAlphaToColor(1f))
        assertEquals(Color.BLACK, Color.BLACK.applyAlphaToColor(100f))
        assertEquals(Color.BLACK, Color.BLACK.applyAlphaToColor(-100f))
        assertEquals(Color.argb(128, 0, 0, 255), Color.BLUE.applyAlphaToColor(0.5f))
    }

    @Test
    @Throws(Exception::class)
    fun getEnabledStateColorResIdTest() {
        assertThat(
            getEnabledStateColorResId(isEnabled = true),
            equalTo(R.color.button_blue_default)
        )
        assertThat(
            getEnabledStateColorResId(isEnabled = false),
            equalTo(R.color.button_blue_disabled)
        )
    }

    @Test
    @Throws(Exception::class)
    fun convertDpToPxTest() {
        val density: Float = TestAppTools.applicationContext.resources.displayMetrics.density

        assertEquals((20 * density).toInt(), convertDpToPx(20f))
    }
}
