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
package com.saltedge.authenticator.features.actions

import com.saltedge.authenticator.R
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_API_RESPONSE
import com.saltedge.authenticator.sdk.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.sdk.model.response.SubmitActionData
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.testTools.TestAppTools
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SubmitActionPresenterTest {

    @Test
    @Throws(Exception::class)
    fun constructorTest() {
        val presenter = createPresenter(viewContract = null)

        Assert.assertNull(presenter.viewContract)

        presenter.viewContract = mockView

        Assert.assertNotNull(presenter.viewContract)
    }

    @Test
    @Throws(Exception::class)
    fun getTitleResIdTest() {
        val presenter = createPresenter(viewContract = mockView)

        Assert.assertThat(
            presenter.getTitleResId(),
            CoreMatchers.equalTo(R.string.action_authentication)
        )
    }

    /**
     * Show complete view when authorizationId and connectionId are empty
     */
    @Test
    @Throws(Exception::class)
    fun onActionInitSuccessCase1() {
        val presenter = createPresenter(viewContract = mockView)
        val connectUrlData = SubmitActionData(
            success = true,
            authorizationId = "",
            connectionId = ""
        )
        presenter.onActionInitSuccess(response = connectUrlData)


        Mockito.verify(mockView).setProcessingVisibility(false)
        Mockito.verify(mockView).updateCompleteViewContent(
            iconResId = R.drawable.ic_complete_ok_70,
            completeTitleResId = R.string.action_feature_title,
            completeMessageResId = R.string.action_feature_description,
            mainActionTextResId = R.string.actions_proceed
        )
    }

    /**
     * Test onActionInitSuccess when success is true and connectionId with authorizationId are not empty
     */
    @Test
    @Throws(Exception::class)
    fun onActionInitSuccessCase2() {
        val presenter = createPresenter(viewContract = mockView)

        val connectUrlData = SubmitActionData(
            success = true,
            authorizationId = "authorizationId",
            connectionId = "connectionId"
        )
        presenter.onActionInitSuccess(response = connectUrlData)

        Mockito.verify(mockView).closeView()
        Mockito.verify(mockView).setResultAuthorizationIdentifier(
            authorizationIdentifier = AuthorizationIdentifier(
                authorizationID = "authorizationId",
                connectionID = "connectionId"
            )
        )
    }

    /**
     * Test onActionInitSuccess when success is false and connectionId with authorizationId are empty
     */
    @Test
    @Throws(Exception::class)
    fun onConnectionInitSuccessTestCase3() {
        val presenter = createPresenter(viewContract = mockView)
        val actionData = SubmitActionData(
            success = false,
            connectionId = "",
            authorizationId = ""
        )
        presenter.onActionInitSuccess(response = actionData)

        Mockito.never()
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionInitFailureTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.onActionInitFailure(
            error = ApiErrorData(
                errorMessage = "test error",
                errorClassName = ERROR_CLASS_API_RESPONSE
            )
        )

        Mockito.verify(mockView).setProcessingVisibility(false)
        Mockito.verify(mockView).updateCompleteViewContent(
            iconResId = R.drawable.ic_auth_error_70,
            completeTitleResId = R.string.action_error_title,
            completeMessageResId = R.string.action_error_description,
            mainActionTextResId = R.string.actions_try_again
        )
        Mockito.verify(mockView).showErrorAndFinish("test error")
    }

    private val mockConnectionsRepository = Mockito.mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = Mockito.mock(KeyStoreManagerAbs::class.java)
    private val mockApiManager = Mockito.mock(AuthenticatorApiManagerAbs::class.java)
    private val mockView = Mockito.mock(SubmitActionContract.View::class.java)

    private fun createPresenter(viewContract: SubmitActionContract.View? = null): SubmitActionPresenter {
        return SubmitActionPresenter(
            appContext = TestAppTools.applicationContext,
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            apiManager = mockApiManager
        ).apply {
            this.viewContract = viewContract
        }
    }
}
