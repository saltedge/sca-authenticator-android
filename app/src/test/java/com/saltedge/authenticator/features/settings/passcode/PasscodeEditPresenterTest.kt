/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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

import android.view.View
import com.saltedge.authenticator.R
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.tools.PasscodeToolsAbs
import com.saltedge.authenticator.widget.passcode.PasscodeInputMode
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class PasscodeEditPresenterTest {

    private val mockPasscodeTools = Mockito.mock(PasscodeToolsAbs::class.java)
    private var doneSignal: CountDownLatch? = null
    private var viewModel: PasscodeEditViewModel

    init {
        Mockito.doReturn("1357").`when`(mockPasscodeTools).getPasscode()
        viewModel = PasscodeEditViewModel(mockPasscodeTools)
    }

    @Test
    @Throws(Exception::class)
    fun onLifecycleStartTest() {
        viewModel.onLifecycleStart()

        assertThat(viewModel.initialPasscode.value, equalTo("1357"))
        assertThat(viewModel.passcodeInputMode.value, equalTo(PasscodeInputMode.CHECK_PASSCODE))
        assertThat(viewModel.titleRes.value, equalTo(R.string.settings_passcode_input_current))
    }

    @Test
    @Throws(Exception::class)
    fun onInputValidPasscodeTest() {
        viewModel.onInputValidPasscode()

        assertThat(viewModel.passcodeInputMode.value, equalTo(PasscodeInputMode.NEW_PASSCODE))
        assertThat(viewModel.titleRes.value, equalTo(R.string.settings_input_new_passcode))
    }

    @Test
    @Throws(Exception::class)
    fun onNewPasscodeEnteredTest() {
        viewModel.onNewPasscodeEntered(PasscodeInputMode.CONFIRM_PASSCODE, "1111")

        assertThat(viewModel.titleRes.value, equalTo(R.string.passcode_confirm_passcode))
    }

    @Test
    @Throws(Exception::class)
    fun onNewPasscodeConfirmedTest() {
        Mockito.doReturn(true).`when`(mockPasscodeTools).savePasscode(passcode = "9753")
        viewModel.loaderVisibility.observeForever {
            if (viewModel.loaderVisibility.value == View.GONE) doneSignal!!.countDown()
        }

        doneSignal = CountDownLatch(1)
        viewModel.onNewPasscodeConfirmed(passcode = "9753")
        doneSignal!!.await(3, TimeUnit.SECONDS)

        Mockito.verify(mockPasscodeTools).savePasscode(passcode = "9753")
    }

    //TODO: check result fun savePasscode(passcodeTools, passcode)
//    @Test
//    @Throws(Exception::class)
//    fun passcodeSavedWithResultTestCase1() {
//        viewModel.passcodeSavedWithResult(result = true)
//
//        assertThat(viewModel.infoEvent.value, equalTo(ViewModelEvent(R.string.settings_passcode_success)))
//        assertThat(viewModel.closeViewEvent.value, equalTo(ViewModelEvent(Unit)))
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun passcodeSavedWithResultTestCase2() {
//        viewModel.passcodeSavedWithResult(result = false)
//
//        assertThat(viewModel.warningEvent.value, equalTo(ViewModelEvent(R.string.errors_contact_support)))
//    }

    @Test
    @Throws(Exception::class)
    fun onPasscodeInputCanceledByUserTest() {
        viewModel.onPasscodeInputCanceledByUser()

        assertThat(viewModel.closeViewEvent.value, equalTo(ViewModelEvent(Unit)))
    }
}
