/*
 * Copyright (c) 2019 Salt Edge Inc.
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
