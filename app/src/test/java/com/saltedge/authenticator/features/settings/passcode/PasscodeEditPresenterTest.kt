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

import com.saltedge.authenticator.R
import com.saltedge.authenticator.tools.PasscodeToolsAbs
import com.saltedge.authenticator.widget.passcode.PasscodeEditView
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class PasscodeEditPresenterTest {

    private val mockView = Mockito.mock(PasscodeEditContract.View::class.java)
    private val mockPasscodeTools = Mockito.mock(PasscodeToolsAbs::class.java)
    private var doneSignal: CountDownLatch? = null

    @Test
    @Throws(Exception::class)
    fun constructorTest() {
        Assert.assertNull(createPresenter(viewContract = null).viewContract)
        Assert.assertNotNull(createPresenter(viewContract = mockView).viewContract)
    }

    @Test
    @Throws(Exception::class)
    fun onViewCreatedTest() {
        Mockito.doReturn("1357").`when`(mockPasscodeTools).getPasscode()
        val presenter = createPresenter(viewContract = null)
        presenter.onViewCreated()

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.viewContract = mockView
        presenter.onViewCreated()

        Mockito.verify(mockView).initInputMode(
            mode = PasscodeEditView.InputMode.CHECK_PASSCODE,
            passcode = "1357"
        )
        Mockito.verify(mockView).updateViewContent(
            titleTextResId = R.string.settings_passcode_input_current,
            positiveActionTextResId = R.string.actions_next
        )
    }

    @Test
    @Throws(Exception::class)
    fun enteredCurrentPasscodeTest() {
        val presenter = createPresenter(viewContract = null)
        presenter.enteredCurrentPasscode()

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.viewContract = mockView
        presenter.enteredCurrentPasscode()

        Mockito.verify(mockView).initInputMode(
            mode = PasscodeEditView.InputMode.NEW_PASSCODE,
            passcode = ""
        )
        Mockito.verify(mockView).updateViewContent(
            titleTextResId = R.string.settings_passcode_input_new,
            positiveActionTextResId = R.string.actions_next
        )
    }

    @Test
    @Throws(Exception::class)
    fun enteredNewPasscodeTest() {
        val presenter = createPresenter(viewContract = null)
        presenter.enteredNewPasscode(PasscodeEditView.InputMode.REPEAT_NEW_PASSCODE)

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.viewContract = mockView
        presenter.enteredNewPasscode(PasscodeEditView.InputMode.REPEAT_NEW_PASSCODE)

        Mockito.verify(mockView).updateViewContent(
            titleTextResId = R.string.settings_passcode_repeat_new,
            positiveActionTextResId = android.R.string.ok
        )
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun newPasscodeConfirmedTest() {
        Mockito.doReturn(true).`when`(mockPasscodeTools).savePasscode(passcode = "9753")
        val presenter = createPresenter(viewContract = object : PasscodeEditContract.View {
            override fun showInfo(messageResId: Int) {}
            override fun initInputMode(mode: PasscodeEditView.InputMode, passcode: String) {}
            override fun updateViewContent(titleTextResId: Int, positiveActionTextResId: Int) {}
            override fun showProgress() {}
            override fun closeView() {}
            override fun showWarning(messageResId: Int) {}
            override fun hideProgress() {
                doneSignal!!.countDown()
            }
        })
        doneSignal = CountDownLatch(1)
        presenter.newPasscodeConfirmed(passcode = "9753")
        doneSignal!!.await(3, TimeUnit.SECONDS)

        Mockito.verify(mockPasscodeTools).savePasscode(passcode = "9753")
    }

    @Test
    @Throws(Exception::class)
    fun passcodeSavedWithResultTest() {
        val presenter = createPresenter(viewContract = null)
        presenter.passcodeSavedWithResult(result = true)

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.viewContract = mockView
        presenter.passcodeSavedWithResult(result = true)

        Mockito.verify(mockView).hideProgress()
        Mockito.verify(mockView).closeView()

        Mockito.clearInvocations(mockView)
        presenter.passcodeSavedWithResult(result = false)

        Mockito.verify(mockView).hideProgress()
        Mockito.verify(mockView).showWarning(R.string.errors_contact_support)
    }

    private fun createPresenter(viewContract: PasscodeEditContract.View? = null): PasscodeEditViewModel {
        return PasscodeEditViewModel(mockPasscodeTools).apply { this.viewContract = viewContract }
    }
}
