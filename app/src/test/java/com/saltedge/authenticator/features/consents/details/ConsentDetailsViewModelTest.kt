/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.consents.details

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.saltedge.android.test_tools.ViewModelTest
import com.saltedge.authenticator.TestAppTools
import com.saltedge.authenticator.TestFactory
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.api.model.ConsentSharedData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.tools.toDateFormatString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import java.lang.ref.WeakReference

@RunWith(RobolectricTestRunner::class)
class ConsentDetailsViewModelTest : ViewModelTest() {

    private lateinit var viewModel: ConsentDetailsViewModel
    private lateinit var interactor: ConsentDetailsInteractor
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = mock(KeyManagerAbs::class.java)
    private val mockApiManagerV1 = mock(AuthenticatorApiManagerAbs::class.java)
    private val mockApiManagerV2 = mock(ScaServiceClientAbs::class.java)
    private lateinit var testFactory: TestFactory

    @Before
    fun setUp() {
        testFactory = TestFactory()
        testFactory.mockConnections(mockConnectionsRepository)
        testFactory.mockRichConnections(mockKeyStoreManager)

        interactor = ConsentDetailsInteractor(
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            v1ApiManager = mockApiManagerV1,
            v2ApiManager = mockApiManagerV2,
        )
        viewModel = ConsentDetailsViewModel(
            weakContext = WeakReference(context),
            interactor = interactor
        )
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase1() {
        //given arguments
        val consent = testFactory.v1AispConsentData.copy(
            expiresAt = DateTime.now().plusDays(1)
        )
        val arguments = Bundle().apply {
            putString(KEY_GUID, testFactory.connection1.guid)
            putSerializable(KEY_DATA, consent)
        }

        //when
        viewModel.setInitialData(arguments)

        //then
        assertThat(viewModel.fragmentTitle.value, equalTo("tppName111"))
        assertThat(viewModel.daysLeft.value, equalTo("1 day left"))
        assertThat(viewModel.consentTitle.value, equalTo("Access to account information"))
        assertThat(viewModel.consentDescription.value.toString(), equalTo(
            "Consent granted to tppName111 application on the following accounts from Demobank1"
        ))
        assertThat(viewModel.consentGranted.value, equalTo(
            "Granted: ${consent.createdAt.toDateFormatString(TestAppTools.applicationContext)}"
        ))
        assertThat(viewModel.consentExpires.value, equalTo(
            "Expires: ${consent.expiresAt.toDateFormatString(TestAppTools.applicationContext)}"
        ))
        assertThat(viewModel.accounts.value, equalTo(emptyList()))
        assertThat(viewModel.sharedDataVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.sharedBalanceVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.sharedTransactionsVisibility.value, equalTo(View.VISIBLE))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase2() {
        //given arguments pisp_future
        val consent = testFactory.v1PispFutureConsentData.copy(
            sharedData = ConsentSharedData(balance = false, transactions = false)
        )
        val arguments = Bundle().apply {
            putString(KEY_GUID, testFactory.connection1.guid)
            putSerializable(KEY_DATA, consent)
        }

        //when
        viewModel.setInitialData(arguments)

        //then
        assertThat(viewModel.consentTitle.value, equalTo("Consent for future payment"))
        assertThat(viewModel.sharedDataVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.sharedBalanceVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.sharedTransactionsVisibility.value, equalTo(View.GONE))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase3() {
        //given arguments pisp_recurring
        val consent = testFactory.v1PispRecurringConsentData.copy(
            sharedData = null
        )
        val arguments = Bundle().apply {
            putString(KEY_GUID, testFactory.connection1.guid)
            putSerializable(KEY_DATA, consent)
        }

        //when
        viewModel.setInitialData(arguments)

        //then
        assertThat(viewModel.consentTitle.value, equalTo("Consent for recurring payment"))
        assertThat(viewModel.sharedDataVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.sharedBalanceVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.sharedTransactionsVisibility.value, equalTo(View.GONE))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase4() {
        //given arguments with invalid guid
        val arguments = Bundle().apply {
            putSerializable(KEY_DATA, testFactory.v1AispConsentData)
        }

        //when
        viewModel.setInitialData(arguments)

        //then
        assertThat(viewModel.fragmentTitle.value, equalTo("Active Consent"))
        assertThat(viewModel.daysLeft.value, equalTo(""))
        assertThat(viewModel.consentTitle.value, equalTo(""))
        assertThat(viewModel.consentDescription.value.toString(), equalTo(""))
        assertThat(viewModel.consentGranted.value, equalTo(""))
        assertThat(viewModel.consentExpires.value, equalTo(""))
        Assert.assertNull(viewModel.accounts.value)
        assertThat(viewModel.sharedDataVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.sharedBalanceVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.sharedTransactionsVisibility.value, equalTo(View.GONE))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase5() {
        //given arguments with invalid params
        val arguments = Bundle()

        //when
        viewModel.setInitialData(arguments)

        //then
        assertThat(viewModel.fragmentTitle.value, equalTo("Active Consent"))
        assertThat(viewModel.daysLeft.value, equalTo(""))
        assertThat(viewModel.consentTitle.value, equalTo(""))
        assertThat(viewModel.consentDescription.value.toString(), equalTo(""))
        assertThat(viewModel.consentGranted.value, equalTo(""))
        assertThat(viewModel.consentExpires.value, equalTo(""))
        Assert.assertNull(viewModel.accounts.value)
        assertThat(viewModel.sharedDataVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.sharedBalanceVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.sharedTransactionsVisibility.value, equalTo(View.GONE))
    }

    @Test
    @Throws(Exception::class)
    fun onRevokeClickTest() {
        //given
        val arguments = Bundle().apply {
            putString(KEY_GUID, testFactory.connection1.guid)
            putSerializable(KEY_DATA, testFactory.v1AispConsentData)
        }
        viewModel.setInitialData(arguments)

        //when
        viewModel.onRevokeActionClick()

        //then
        assertThat(
            viewModel.revokeQuestionEvent.value!!.peekContent(),
            equalTo(
            "tppName111 service that is provided to you may be interrupted. Are you sure you want to revoke consent?"
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun onRevokeConfirmedTestCase1() {
        //given
        val arguments = Bundle().apply {
            putString(KEY_GUID, testFactory.connection1.guid)
            putSerializable(KEY_DATA, testFactory.v1AispConsentData)
        }
        viewModel.setInitialData(arguments)

        //when
        viewModel.onRevokeConfirmedByUser()

        //then
        verify(mockApiManagerV1).revokeConsent(
            consentId = testFactory.v1AispConsentData.id,
            connectionAndKey = testFactory.richConnection1,
            resultCallback = interactor
        )
        Mockito.verifyNoInteractions(mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun onRevokeConfirmedTestCase2() {
        //given
        val arguments = Bundle().apply {
            putString(KEY_GUID, testFactory.connection2.guid)
            putSerializable(KEY_DATA, testFactory.v2ConsentData)
        }
        viewModel.setInitialData(arguments)

        //when
        viewModel.onRevokeConfirmedByUser()

        //then
        verify(mockApiManagerV2).revokeConsent(
            consentID = testFactory.v2ConsentData.id,
            richConnection = testFactory.richConnection2,
            callback = interactor
        )
        Mockito.verifyNoInteractions(mockApiManagerV1)
    }

    @Test
    @Throws(Exception::class)
    fun onRevokeConfirmedTestCase3() {
        //given invalid initial data
        viewModel.setInitialData(Bundle())

        //when
        viewModel.onRevokeConfirmedByUser()

        //then
        Mockito.verifyNoInteractions(mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun onConsentRevokeFailureTest() {
        //given
        val apiError = ApiErrorData(errorClassName = "", errorMessage = "message")

        //when
        interactor.onConsentRevokeFailure(apiError)

        //then
        assertThat(viewModel.revokeErrorEvent.value!!.peekContent(), equalTo(apiError.errorMessage))
    }

    @Test
    @Throws(Exception::class)
    fun onConsentRevokeSuccessTest() {
        //given valid response params
        val consentId = "1"

        //when
        interactor.onConsentRevokeSuccess(consentId)

        //then
        assertThat(viewModel.revokeSuccessEvent.value!!.peekContent(), equalTo("1"))
    }
}
