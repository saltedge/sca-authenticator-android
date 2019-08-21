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
package com.saltedge.authenticator.unitTests.tool

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.R
import com.saltedge.authenticator.testTools.TestTools
import com.saltedge.authenticator.tool.validatePasscode
import com.saltedge.authenticator.tool.isPasscodeValid
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ValidationHelperTest {

    @Test
    @Throws(Exception::class)
    fun validatePasscodeTests() {
        assertThat(
            validatePasscode("", TestTools.applicationContext),
            equalTo(TestTools.applicationContext.getString(R.string.errors_empty_passcode))
        )
        assertThat(
            validatePasscode("123", TestTools.applicationContext),
            equalTo("The Passcode should contain between 4 and 16 digits.")
        )
        Assert.assertNull(validatePasscode("1234", TestTools.applicationContext))
        Assert.assertNull(validatePasscode("1234567890123456", TestTools.applicationContext))
        assertThat(
            validatePasscode("12345678901234567", TestTools.applicationContext),
            equalTo("The Passcode should contain between 4 and 16 digits.")
        )
    }

    @Test
    @Throws(Exception::class)
    fun isPasscodeValidTest() {
        Assert.assertFalse(isPasscodeValid(""))
        Assert.assertFalse(isPasscodeValid("123"))
        Assert.assertTrue(isPasscodeValid("1234"))
        Assert.assertTrue(isPasscodeValid("123456"))
        Assert.assertTrue(isPasscodeValid("1234567890123456"))
        Assert.assertFalse(isPasscodeValid("12345678901234567"))
    }
}
