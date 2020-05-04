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
package com.saltedge.authenticator.unitTests.tool.secure

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.instrumentationTestTools.TestTools.applicationContext
import com.saltedge.authenticator.models.repository.PreferenceRepository
import com.saltedge.authenticator.tools.PasscodeTools
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PasscodeToolsTest {

    @Test
    @Throws(Exception::class)
    fun saveNewPasscodeTest() {
        PasscodeTools.replacePasscodeKey(applicationContext)
        PreferenceRepository.encryptedPasscode = ""

        Assert.assertTrue(PreferenceRepository.encryptedPasscode.isEmpty())
        Assert.assertTrue(PasscodeTools.savePasscode("1"))
        Assert.assertTrue(PasscodeTools.savePasscode("12"))
        Assert.assertTrue(PasscodeTools.savePasscode("123"))
        Assert.assertTrue(PasscodeTools.savePasscode("1234"))
        Assert.assertTrue(PreferenceRepository.encryptedPasscode.isNotEmpty())

        assertThat(PasscodeTools.getPasscode(), equalTo("1234"))
    }

    @Test
    @Throws(Exception::class)
    fun getSavedPasscodeTest() {
        PasscodeTools.replacePasscodeKey(applicationContext)

        Assert.assertTrue(PasscodeTools.savePasscode("1234"))
        assertThat(PreferenceRepository.encryptedPasscode, not(equalTo("1234")))
        assertThat(PasscodeTools.getPasscode(), equalTo("1234"))
    }
}
