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
package com.saltedge.authenticator.features.authorizations.confirm

import com.saltedge.authenticator.features.authorizations.confirmPasscode.ConfirmPasscodeDialog
import com.saltedge.authenticator.features.authorizations.confirmPasscode.PasscodePromptCallback
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConfirmPasscodeDialogTest {

    private val mockView = Mockito.mock(PasscodePromptCallback::class.java)

    @Test
    @Throws(Exception::class)
    fun newInstanceTest() {
        val arguments = ConfirmPasscodeDialog.newInstance(resultCallback = mockView).arguments

        Assert.assertNull(arguments)
    }
}
