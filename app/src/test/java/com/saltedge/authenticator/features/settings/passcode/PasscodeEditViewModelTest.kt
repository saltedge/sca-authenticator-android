/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.settings.passcode

import com.saltedge.android.test_tools.CoroutineViewModelTest
import com.saltedge.authenticator.R
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.tools.PasscodeToolsAbs
import com.saltedge.authenticator.widget.passcode.PasscodeInputMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class PasscodeEditViewModelTest : CoroutineViewModelTest() {

    private val mockPasscodeTools = Mockito.mock(PasscodeToolsAbs::class.java)
    private lateinit var viewModel: PasscodeEditViewModel

    @Before
    override fun setUp() {
        super.setUp()
        Mockito.doReturn("1357").`when`(mockPasscodeTools).getPasscode()
        viewModel = PasscodeEditViewModel(passcodeTools = mockPasscodeTools, defaultDispatcher = testDispatcher)
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

        viewModel.onNewPasscodeConfirmed(passcode = "9753")

        Mockito.verify(mockPasscodeTools).savePasscode(passcode = "9753")
    }

    @Test
    @Throws(Exception::class)
    fun passcodeSavedWithResultTestCase1() {
        Mockito.doReturn(true).`when`(mockPasscodeTools).savePasscode(passcode = "2222")

        viewModel.onNewPasscodeConfirmed(passcode = "2222")

        assertThat(viewModel.infoEvent.value, equalTo(ViewModelEvent(R.string.settings_passcode_success)))
        assertThat(viewModel.closeViewEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun passcodeSavedWithResultTestCase2() {
        viewModel.onNewPasscodeConfirmed(passcode = "1111")

        assertThat(viewModel.warningEvent.value, equalTo(ViewModelEvent(R.string.errors_contact_support)))
    }

    @Test
    @Throws(Exception::class)
    fun onPasscodeInputCanceledByUserTest() {
        viewModel.onPasscodeInputCanceledByUser()

        assertThat(viewModel.closeViewEvent.value, equalTo(ViewModelEvent(Unit)))
    }
}
