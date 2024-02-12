/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.widget.biometric

import android.os.Bundle
import com.saltedge.authenticator.R
import com.saltedge.authenticator.TestAppTools
import com.saltedge.authenticator.core.api.KEY_DESCRIPTION
import com.saltedge.authenticator.core.api.KEY_TITLE
import com.saltedge.authenticator.core.tools.biometric.BiometricToolsAbs
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import javax.crypto.Cipher

@RunWith(RobolectricTestRunner::class)
class BiometricsInputPresenterTest {

    private val mockBiometricTools = Mockito.mock(BiometricToolsAbs::class.java)
    private val mockView = Mockito.mock(BiometricsInputContract.View::class.java)
    private val mockCipher = Mockito.mock(Cipher::class.java)

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase1() {
        //given
        val arguments: Bundle? = null
        given(mockBiometricTools.createFingerprintCipher()).willReturn(null)
        val presenter = createPresenter(viewContract = mockView)

        //when
        presenter.setInitialData(arguments)

        //then
        Assert.assertFalse(presenter.initialized)
        assertThat(presenter.titleRes, equalTo(R.string.errors_error))
        assertThat(presenter.descriptionRes, equalTo(R.string.errors_fingerprint_init))
        assertThat(presenter.negativeActionTextRes, equalTo(R.string.actions_cancel))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase2() {
        //given
        val arguments: Bundle? = Bundle().apply {
            putInt(KEY_TITLE, R.string.app_name)
            putInt(KEY_DESCRIPTION, R.string.app_name_in_two_lines)
        }
        given(mockBiometricTools.createFingerprintCipher()).willReturn(null)
        val presenter = createPresenter(viewContract = mockView)

        //when
        presenter.setInitialData(arguments)

        //then
        Assert.assertFalse(presenter.initialized)
        assertThat(presenter.titleRes, equalTo(R.string.errors_error))
        assertThat(presenter.descriptionRes, equalTo(R.string.errors_fingerprint_init))
        assertThat(presenter.negativeActionTextRes, equalTo(R.string.actions_cancel))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase3() {
        //given
        val arguments: Bundle? = null

        given(mockBiometricTools.createFingerprintCipher()).willReturn(mockCipher)
        val presenter = createPresenter(viewContract = mockView)

        //when
        presenter.setInitialData(arguments)

        //then
        Assert.assertTrue(presenter.initialized)
        assertThat(presenter.titleRes, equalTo(R.string.fingerprint_title))
        assertThat(presenter.descriptionRes, equalTo(R.string.fingerprint_touch_sensor))
        assertThat(presenter.negativeActionTextRes, equalTo(R.string.actions_cancel))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase4() {
        //given
        val arguments: Bundle? = Bundle().apply {
            putInt(KEY_TITLE, R.string.app_name)
            putInt(KEY_DESCRIPTION, R.string.app_name_in_two_lines)
        }
        given(mockBiometricTools.createFingerprintCipher()).willReturn(mockCipher)//Cipher.getInstance("AES/CBC/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
        val presenter = createPresenter(viewContract = mockView)

        //when
        presenter.setInitialData(arguments)

        //then
        Assert.assertTrue(presenter.initialized)
        assertThat(presenter.titleRes, equalTo(R.string.app_name))
        assertThat(presenter.descriptionRes, equalTo(R.string.app_name_in_two_lines))
        assertThat(presenter.negativeActionTextRes, equalTo(R.string.actions_cancel))
    }

    @Test
    @Throws(Exception::class)
    fun onAuthenticationSucceededTest() {
        val presenter = createPresenter(viewContract = mockView)

        presenter.onDialogResume(TestAppTools.applicationContext)
        presenter.onAuthenticationSucceeded(null)

        Mockito.verify(mockView).sendAuthSuccessResult()
    }

    @Test
    @Throws(Exception::class)
    fun onAuthenticationFailedTest() {
        val presenter = createPresenter(viewContract = mockView)

        presenter.onDialogResume(TestAppTools.applicationContext)
        presenter.onAuthenticationFailed()

        Mockito.verify(mockView).updateStatusView(
            imageResId = R.drawable.ic_fingerprint_error,
            textColorResId = R.color.red,
            textResId = R.string.errors_fingerprint_not_recognized,
            animateText = true
        )
    }

    @Test
    @Throws(Exception::class)
    fun onDialogPauseTest() {
        val presenter = createPresenter(viewContract = mockView)

        presenter.onDialogResume(TestAppTools.applicationContext)
        presenter.onAuthenticationSucceeded(null)

        Mockito.verify(mockView).updateStatusView(
            imageResId = R.drawable.ic_fingerprint_confirmed,
            textColorResId = R.color.primary,
            textResId = R.string.fingerprint_confirmed,
            animateText = false
        )

        presenter.onDialogPause()
        presenter.onAuthenticationSucceeded(null)

        Mockito.verify(mockView).updateStatusView(
            imageResId = R.drawable.ic_fingerprint_confirmed,
            textColorResId = R.color.primary,
            textResId = R.string.fingerprint_confirmed,
            animateText = false
        )
    }

    @Test
    @Throws(Exception::class)
    fun onAuthenticationErrorTest() {
        createPresenter(viewContract = mockView).onAuthenticationError(
            errMsgId = 10,
            errString = ""
        )

        Mockito.never()
    }

    @Test
    @Throws(Exception::class)
    fun onAuthenticationHelpTest() {
        createPresenter(viewContract = mockView).onAuthenticationHelp(
            helpMsgId = 1,
            helpString = ""
        )

        Mockito.never()
    }

    private fun createPresenter(viewContract: BiometricsInputContract.View? = null): BiometricsInputPresenter {
        return BiometricsInputPresenter(
            biometricTools = mockBiometricTools,
            contract = viewContract
        )
    }
}
