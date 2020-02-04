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
package com.saltedge.authenticator.features.connections.actions

import com.saltedge.authenticator.R
import com.saltedge.authenticator.model.db.Connection
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_API_RESPONSE
import com.saltedge.authenticator.sdk.model.ApiErrorData
import com.saltedge.authenticator.sdk.model.response.ActionData
import com.saltedge.authenticator.sdk.tools.ActionDeepLinkData
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.testTools.TestAppTools
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActionPresenterTest {

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
            CoreMatchers.equalTo(R.string.connections_new_connection)
        )
    }

    @Test
    @Throws(Exception::class)
    fun showProcessingViewTest() {
        val presenter = createPresenter(viewContract = mockView)

        Assert.assertFalse(presenter.showCompleteView)

        val connectUrlData = ActionData(
            success = true,
            authorizationId = "authorizationId",
            connectionId = "connectionId"
        )
        presenter.onActionInitSuccess(response = connectUrlData)

        Assert.assertTrue(presenter.showCompleteView)
    }

    /**
     * Test onDestroyView() when guid is not empty and accessToken is empty
     */
    @Test
    @Throws(Exception::class)
    fun onDestroyViewTestCase1() {
        val connection = Connection().apply {
            guid = "guid2"
            accessToken = ""
            code = "demobank1"
            name = "Demobank1"
        }
        val actionDeepLinkData = ActionDeepLinkData(
            actionUuid = "actionUuid",
            connectUrl = "connectUrl",
            returnTo = "returnTo"
        )
        Mockito.`when`(mockConnectionsRepository.getByGuid("guid2")).thenReturn(connection)
        val presenter = createPresenter()
        presenter.setInitialData(
            connectionGuid = "guid2",
            actionDeepLinkData = actionDeepLinkData
        )
        presenter.onDestroyView()

        Mockito.verify(mockKeyStoreManager).deleteKeyPair("guid2")
    }

    /**
     * Test onDestroyView() when guid and accessToken is not empty
     */
    @Test
    @Throws(Exception::class)
    fun onDestroyViewTestCase2() {
        val connection = Connection().apply {
            guid = "guid2"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        val actionDeepLinkData = ActionDeepLinkData(
            actionUuid = "actionUuid",
            connectUrl = "connectUrl",
            returnTo = "returnTo"
        )
        Mockito.`when`(mockConnectionsRepository.getByGuid("guid2")).thenReturn(connection)
        val presenter = createPresenter()
        presenter.setInitialData(
            connectionGuid = "",
            actionDeepLinkData = actionDeepLinkData
        )
        presenter.onDestroyView()

        Mockito.never()
    }

    /**
     * Test onDestroyView() when guid and accessToken are empty
     */
    @Test
    @Throws(Exception::class)
    fun onDestroyViewTestCase3() {
        val connection = Connection().apply {
            guid = "guid2"
            accessToken = ""
            code = "demobank1"
            name = "Demobank1"
        }
        val actionDeepLinkData = ActionDeepLinkData(
            actionUuid = "actionUuid",
            connectUrl = "connectUrl",
            returnTo = "returnTo"
        )
        Mockito.`when`(mockConnectionsRepository.getByGuid("guid2")).thenReturn(connection)
        val presenter = createPresenter()
        presenter.setInitialData(
            connectionGuid = "guid2",
            actionDeepLinkData = actionDeepLinkData
        )
        presenter.onDestroyView()

        Mockito.never()
    }

    /**
     * Test onDestroyView() when accessToken is not empty and guid is empty
     */
    @Test
    @Throws(Exception::class)
    fun onDestroyViewTestCase4() {
        val connection = Connection().apply {
            guid = "guid2"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        val actionDeepLinkData = ActionDeepLinkData(
            actionUuid = "actionUuid",
            connectUrl = "connectUrl",
            returnTo = "returnTo"
        )
        Mockito.`when`(mockConnectionsRepository.getByGuid("guid2")).thenReturn(connection)
        val presenter = createPresenter()
        presenter.setInitialData(
            connectionGuid = "guid2",
            actionDeepLinkData = actionDeepLinkData
        )
        presenter.onDestroyView()

        Mockito.never()
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionInitSuccessTestCase1() {
        val presenter = createPresenter(viewContract = mockView)
        val actionData = ActionData(
            success = true,
            connectionId = "connectionId",
            authorizationId = "authorizationId"
        )
        presenter.onActionInitSuccess(response = actionData)

        Mockito.verify(mockView).updateViewsContent()
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionInitSuccessTestCase2() {
        val presenter = createPresenter(viewContract = mockView)
        val actionData = ActionData(
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

        Mockito.verify(mockView).showErrorAndFinish("test error")
    }

    private val mockConnectionsRepository = Mockito.mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = Mockito.mock(KeyStoreManagerAbs::class.java)
    private val mockApiManager = Mockito.mock(AuthenticatorApiManagerAbs::class.java)
    private val mockView = Mockito.mock(ActionContract.View::class.java)

    private fun createPresenter(viewContract: ActionContract.View? = null): ActionPresenter {
        return ActionPresenter(
            appContext = TestAppTools.applicationContext,
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            apiManager = mockApiManager
        ).apply { this.viewContract = viewContract }
    }
}
