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
package com.saltedge.authenticator.features.settings.passcode

import com.saltedge.authenticator.features.settings.passcode.saver.PasscodeSaveResultListener
import com.saltedge.authenticator.features.settings.passcode.saver.PasscodeSaver
import com.saltedge.authenticator.tool.secure.PasscodeToolsAbs
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class PasscodeSaverTest : PasscodeSaveResultListener {

    @Before
    @Throws(Exception::class)
    fun setUp() {
        doneSignal = CountDownLatch(1)
    }

    @After
    fun tearDown() {
        saver?.cancel(true)
    }

    /**
     * execute PasscodeSaver with no arguments
     */
    @Test
    @Throws(Exception::class)
    fun asyncTaskTestCase1() {
        assertFalse(passcodeResult)

        saver = PasscodeSaver(passcodeTools = mockPasscodeTools, callback = this)
        saver?.execute()
        doneSignal!!.await(5, TimeUnit.SECONDS)

        assertTrue(passcodeResult)
        Mockito.verifyNoMoreInteractions(mockPasscodeTools)
    }

    /**
     * execute PasscodeSaver with argument
     */
    @Test
    @Throws(Exception::class)
    fun asyncTaskTestCase2() {
        assertFalse(passcodeResult)

        PasscodeSaver(passcodeTools = mockPasscodeTools, callback = this).runNewTask("1234")
        doneSignal!!.await(5, TimeUnit.SECONDS)

        assertTrue(passcodeResult)
        Mockito.verify(mockPasscodeTools).savePasscode(passcode = "1234")
    }

    override fun passcodeSavedWithResult(result: Boolean) {
        passcodeResult = true
        doneSignal?.countDown()
    }

    private val mockPasscodeTools = Mockito.mock(PasscodeToolsAbs::class.java)
    private var saver: PasscodeSaver? = null
    private var doneSignal: CountDownLatch? = null
    private var passcodeResult: Boolean = false
}
