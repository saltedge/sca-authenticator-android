/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2019 Salt Edge Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 or later.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * For the additional permissions granted for Salt Edge Authenticator
 * under Section 7 of the GNU General Public License see THIRD_PARTY_NOTICES.md
 */
package com.saltedge.authenticator.tool

import android.graphics.Color
import com.saltedge.authenticator.R
import com.saltedge.authenticator.testTools.TestAppTools
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
    fun getDisplayHeightTest() {
        assertTrue(AppTools.getDisplayHeight(TestAppTools.applicationContext) > 0)
    }

    @Test
    @Throws(Exception::class)
    fun getDisplayWidthTest() {
        assertTrue(AppTools.getDisplayWidth(TestAppTools.applicationContext) > 0)
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
