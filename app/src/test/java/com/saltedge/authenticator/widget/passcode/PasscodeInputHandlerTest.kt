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
package com.saltedge.authenticator.widget.passcode

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PasscodeInputHandlerTest : PinpadInputHandlerContract {

    @Test
    @Throws(Exception::class)
    fun onKeyClickTestCase1() {
        var handler = PinpadInputHandler(null)
        handler.onKeyClick(PinpadView.Control.DELETE)

        Assert.assertTrue(newPasscodeOutputText.isEmpty())
        Assert.assertFalse(onFingerprintClickAction)

        handler = PinpadInputHandler(this)
        currentPasscodeOutputText = "test output"
        newPasscodeOutputText = ""
        handler.onKeyClick(PinpadView.Control.DELETE)

        assertThat(newPasscodeOutputText, equalTo("test outpu"))
        Assert.assertFalse(onFingerprintClickAction)

        currentPasscodeOutputText = ""
        newPasscodeOutputText = ""
        handler.onKeyClick(PinpadView.Control.DELETE)

        Assert.assertTrue(newPasscodeOutputText.isEmpty())
        Assert.assertFalse(onFingerprintClickAction)
    }

    @Test
    @Throws(Exception::class)
    fun onKeyClickTestCase2() {
        currentPasscodeOutputText = "test output"
        var handler = PinpadInputHandler(null)
        handler.onKeyClick(PinpadView.Control.NUMBER)

        Assert.assertTrue(newPasscodeOutputText.isEmpty())
        Assert.assertFalse(onFingerprintClickAction)

        handler = PinpadInputHandler(this)
        handler.onKeyClick(PinpadView.Control.NUMBER)

        assertThat(newPasscodeOutputText, equalTo("test output"))
        Assert.assertFalse(onFingerprintClickAction)

        handler.onKeyClick(PinpadView.Control.NUMBER, "1")

        assertThat(newPasscodeOutputText, equalTo("test output1"))
        Assert.assertFalse(onFingerprintClickAction)
    }

    @Test
    @Throws(Exception::class)
    fun onKeyClickTestCase3() {
        currentPasscodeOutputText = "test output"
        var handler = PinpadInputHandler(null)
        handler.onKeyClick(PinpadView.Control.FINGER)

        Assert.assertTrue(newPasscodeOutputText.isEmpty())
        Assert.assertFalse(onFingerprintClickAction)

        handler = PinpadInputHandler(this)
        handler.onKeyClick(PinpadView.Control.FINGER)

        Assert.assertTrue(newPasscodeOutputText.isEmpty())
        Assert.assertTrue(onFingerprintClickAction)
    }

    private var currentPasscodeOutputText: String = ""
    private var newPasscodeOutputText: String = ""
    private var onFingerprintClickAction: Boolean = false

    @Before
    fun setUp() {
        currentPasscodeOutputText = ""
        newPasscodeOutputText = ""
        onFingerprintClickAction = false
    }

    override fun getPasscodeOutputText(): String = currentPasscodeOutputText

    override fun setPasscodeOutputText(text: String) {
        newPasscodeOutputText = text
    }

    override fun onFingerprintClickAction() {
        onFingerprintClickAction = true
    }
}
