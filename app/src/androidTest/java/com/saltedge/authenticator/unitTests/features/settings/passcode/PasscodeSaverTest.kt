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
package com.saltedge.authenticator.unitTests.features.settings.passcode

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.features.settings.passcode.saver.PasscodeSaveResultListener
import com.saltedge.authenticator.features.settings.passcode.saver.PasscodeSaver
import com.saltedge.authenticator.model.repository.PreferenceRepository
import com.saltedge.authenticator.testTools.TestTools
import com.saltedge.authenticator.tool.secure.PasscodeTools
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class PasscodeSaverTest : PasscodeSaveResultListener {

    private var saver: PasscodeSaver? = null
    private var doneSignal: CountDownLatch? = null
    private var passcodeResult: Boolean = false

    @Before
    @Throws(Exception::class)
    fun setUp() {
        TestTools.setLocale("en")
        PreferenceRepository.encryptedPasscode = ""
        doneSignal = CountDownLatch(1)
    }

    @After
    fun tearDown() {
        saver?.cancel(true)
    }

    @Test
    @Throws(Exception::class)
    fun asyncTaskTestCase1() {
        assertFalse(passcodeResult)
        assertTrue(PreferenceRepository.encryptedPasscode.isEmpty())

        saver = PasscodeSaver(passcodeTools = PasscodeTools, callback = this)
        saver?.execute()
        doneSignal!!.await(5, TimeUnit.SECONDS)

        assertTrue(passcodeResult)
        assertTrue(PreferenceRepository.encryptedPasscode.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun asyncTaskTestCase2() {
        assertFalse(passcodeResult)
        assertTrue(PreferenceRepository.encryptedPasscode.isEmpty())

        PasscodeTools.replacePasscodeKey()
        PasscodeSaver(passcodeTools = PasscodeTools, callback = this).runNewTask("1234")
        doneSignal!!.await(5, TimeUnit.SECONDS)

        assertTrue(passcodeResult)
        assertThat(PasscodeTools.getPasscode(), equalTo("1234"))
    }

    override fun passcodeSavedWithResult(result: Boolean) {
        passcodeResult = true
        doneSignal?.countDown()
    }
}
