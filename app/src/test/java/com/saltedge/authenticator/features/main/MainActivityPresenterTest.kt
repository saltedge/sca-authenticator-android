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
package com.saltedge.authenticator.features.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_DEEP_LINK
import com.saltedge.authenticator.app.QR_SCAN_REQUEST_CODE
import com.saltedge.authenticator.features.connections.list.convertConnectionsToViewModels
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.constants.KEY_AUTHORIZATION_ID
import com.saltedge.authenticator.sdk.constants.KEY_CONNECTION_ID
import com.saltedge.authenticator.sdk.model.appLink.ActionAppLinkData
import com.saltedge.authenticator.sdk.model.appLink.ConnectAppLinkData
import com.saltedge.authenticator.sdk.model.connection.ConnectionStatus
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivityPresenterTest {

    @Test
    @Throws(Exception::class)
    fun getNavigationIconResourceIdTest() {
        val presenter = MainActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            appContext = context
        )

        Assert.assertNotNull(presenter.viewContract)
        assertThat(
            presenter.getNavigationIcon(isTopNavigationLevel = false),
            equalTo(R.drawable.ic_arrow_back_white_24dp)
        )
        Assert.assertNull(presenter.getNavigationIcon(isTopNavigationLevel = true))
    }

    @Test
    @Throws(Exception::class)
    fun launchInitialFragmentTest_normalMode() {
        val presenter = MainActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            appContext = context
        )

        Mockito.doReturn(false).`when`(mockConnectionsRepository).hasActiveConnections()
        presenter.launchInitialFragment(Intent())

        Mockito.verify(mockView).setSelectedTabbarItemId(R.id.menu_connections)

        Mockito.doReturn(true).`when`(mockConnectionsRepository).hasActiveConnections()
        Mockito.clearInvocations(mockConnectionsRepository)
        presenter.launchInitialFragment(Intent())

        Mockito.verify(mockView).setSelectedTabbarItemId(R.id.menu_authorizations)
    }

    @Test
    @Throws(Exception::class)
    fun launchInitialFragmentTest_quickConfirmMode() {
        val presenter = MainActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            appContext = context
        )
        presenter.launchInitialFragment(
            Intent()
                .putExtra(KEY_CONNECTION_ID, "connectionId1")
                .putExtra(KEY_AUTHORIZATION_ID, "authorizationId1")
        )

        Mockito.verify(mockView).showAuthorizationDetailsView(
            connectionID = "connectionId1", authorizationID = "authorizationId1"
        )
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultTest() {
        val presenter = MainActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            appContext = context
        )
        val intent = Intent().putExtra(
            KEY_DEEP_LINK,
            "authenticator://saltedge.com/connect?configuration=https://saltedge.com/configuration"
        )
        presenter.onActivityResult(
            requestCode = QR_SCAN_REQUEST_CODE,
            resultCode = Activity.RESULT_OK,
            data = intent
        )


        Assert.assertTrue(intent.getStringExtra(KEY_DEEP_LINK)!!.isNotEmpty())
        Mockito.verify(mockView).showConnectProvider(
            connectAppLinkData = ConnectAppLinkData("https://saltedge.com/configuration")
        )

        Mockito.clearInvocations(mockView)
        presenter.onActivityResult(requestCode = 0, resultCode = Activity.RESULT_OK, data = intent)

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.onActivityResult(
            requestCode = 0,
            resultCode = Activity.RESULT_CANCELED,
            data = intent
        )

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.onActivityResult(
            requestCode = 0,
            resultCode = Activity.RESULT_CANCELED,
            data = null
        )

        Mockito.verifyNoMoreInteractions(mockView)
    }

    /**
     * test onNewIntentReceived with empty intent
     * nothing should happen
     */
    @Test
    @Throws(Exception::class)
    fun onNewIntentReceivedTest_case1() {
        val presenter = MainActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            appContext = context
        )

        presenter.onNewIntentReceived(Intent())

        Mockito.verifyNoMoreInteractions(mockView, mockConnectionsRepository)
    }

    /**
     * test onNewIntentReceived where intent has connection id and authorization id
     * should show AuthorizationDetails
     */
    @Test
    @Throws(Exception::class)
    fun onNewIntentReceivedTest_case2() {
        val presenter = MainActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            appContext = context
        )

        presenter.onNewIntentReceived(
            Intent()
                .putExtra(KEY_CONNECTION_ID, "connectionId1")
        )

        Mockito.verifyNoMoreInteractions(mockView, mockConnectionsRepository)

        presenter.onNewIntentReceived(
            Intent()
                .putExtra(KEY_CONNECTION_ID, "connectionId1")
                .putExtra(KEY_AUTHORIZATION_ID, "authorizationId1")
        )

        Mockito.verify(mockView).showAuthorizationDetailsView(
            connectionID = "connectionId1",
            authorizationID = "authorizationId1"
        )
    }

    /**
     * test onNewIntentReceived where intent has invalid deep-link
     */
    @Test
    @Throws(Exception::class)
    fun onNewIntentReceivedTest_case3() {
        val presenter = MainActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            appContext = context
        )

        presenter.onNewIntentReceived(
            Intent().putExtra(KEY_DEEP_LINK, "authenticator://saltedge.com/connect")
        )
        Mockito.verifyNoMoreInteractions(mockView, mockConnectionsRepository)
    }

    /**
     * test onNewIntentReceived where intent has valid deep-link
     * should show AuthorizationDetails
     */
    @Test
    @Throws(Exception::class)
    fun onNewIntentReceivedTest_case4() {
        val presenter = MainActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            appContext = context
        )

        Mockito.clearInvocations(mockView)
        presenter.onNewIntentReceived(
            Intent().putExtra(
                KEY_DEEP_LINK,
                "authenticator://saltedge.com/connect?configuration=https://saltedge.com/configuration"
            )
        )

        Mockito.verify(mockView).setSelectedTabbarItemId(R.id.menu_connections)
        Mockito.verify(mockView).showConnectProvider(
            connectAppLinkData = ConnectAppLinkData("https://saltedge.com/configuration")
        )

        Mockito.clearInvocations(mockView)
        presenter.onNewIntentReceived(
            Intent().putExtra(
                KEY_DEEP_LINK,
                "authenticator://saltedge.com/connect?configuration=https://saltedge.com/configuration&connect_query=1234567890"
            )
        )

        Mockito.verify(mockView).setSelectedTabbarItemId(R.id.menu_connections)
        Mockito.verify(mockView).showConnectProvider(
            connectAppLinkData = ConnectAppLinkData("https://saltedge.com/configuration", "1234567890")
        )
    }

    /**
     * test onNewIntentReceived where intent has valid deep-link for action and connections are empty
     */
    @Test
    @Throws(Exception::class)
    fun onNewIntentReceivedTest_case5() {
        val presenter = MainActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            appContext = context
        )

        presenter.onNewIntentReceived(
            Intent().putExtra(KEY_DEEP_LINK, "authenticator://saltedge.com/action?action_uuid=123456&return_to=http://return.com&connect_url=http://someurl.com")
        )

        Mockito.verify(mockView).showNoConnectionsError()
        Mockito.verify(mockConnectionsRepository).getByConnectUrl("http://someurl.com")
        Mockito.verifyNoMoreInteractions(mockView, mockConnectionsRepository)
    }

    /**
     * test onNewIntentReceived where intent has valid deep-link for action and connections size == 1
     */
    @Test
    @Throws(Exception::class)
    fun onNewIntentReceivedTest_case6() {
        val presenter = MainActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            appContext = context
        )
        val connections = listOf(
            Connection().apply {
                guid = "guid1"
                status = "${ConnectionStatus.ACTIVE}"
                accessToken = "accessToken"
                code = "demobank1"
                name = "Demobank1"
            }
        )

        Mockito.doReturn(connections).`when`(mockConnectionsRepository).getByConnectUrl("http://someurl.com")


        presenter.onNewIntentReceived(
            Intent().putExtra(
                KEY_DEEP_LINK,
                "authenticator://saltedge.com/action?action_uuid=123456&return_to=http://return.com&connect_url=http://someurl.com"
            )
        )

        Mockito.verify(mockView).showSubmitActionFragment(
            connectionGuid = "guid1",
            actionAppLinkData = ActionAppLinkData(
                actionUuid = "123456",
                connectUrl = "http://someurl.com",
                returnTo = "http://return.com"
            )
        )
        Mockito.verify(mockConnectionsRepository).getByConnectUrl("http://someurl.com")
        Mockito.verifyNoMoreInteractions(mockView, mockConnectionsRepository)
    }

    /**
     * test onNewIntentReceived where intent has valid deep-link for action and connections size > 1
     */
    @Test
    @Throws(Exception::class)
    fun onNewIntentReceivedTest_case7() {
        val presenter = MainActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            appContext = context
        )
        val connections = listOf(
            Connection().apply {
                guid = "guid1"
                status = "${ConnectionStatus.ACTIVE}"
                accessToken = "accessToken"
                code = "demobank1"
                name = "Demobank1"
            },
            Connection().apply {
                guid = "guid2"
                status = "${ConnectionStatus.ACTIVE}"
                accessToken = "accessToken"
                code = "demobank2"
                name = "Demobank2"
            }
        )
        val resultMap = connections.convertConnectionsToViewModels(context = context)

        Mockito.doReturn(connections).`when`(mockConnectionsRepository).getByConnectUrl("http://someurl.com")

        presenter.onNewIntentReceived(
            Intent().putExtra(
                KEY_DEEP_LINK,
                "authenticator://saltedge.com/action?action_uuid=123456&return_to=http://return.com&connect_url=http://someurl.com"
            )
        )
        Mockito.verify(mockView).showConnectionsSelectorFragment(resultMap)
        Mockito.verify(mockConnectionsRepository).getByConnectUrl("http://someurl.com")
        Mockito.verifyNoMoreInteractions(mockView, mockConnectionsRepository)
    }


    @Test
    @Throws(Exception::class)
    fun onNavigationItemSelectedTest() {
        val presenter = MainActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            appContext = context
        )

        Assert.assertTrue(presenter.onNavigationItemSelected(R.id.menu_authorizations))

        Mockito.verify(mockView).showAuthorizationsList()

        Mockito.clearInvocations(mockView)
        Assert.assertTrue(presenter.onNavigationItemSelected(R.id.menu_connections))

        Mockito.verify(mockView).showConnectionsList()

        Mockito.clearInvocations(mockView)
        Assert.assertTrue(presenter.onNavigationItemSelected(R.id.menu_settings))

        Mockito.verify(mockView).showSettingsList()

        Mockito.clearInvocations(mockView)
        Assert.assertFalse(presenter.onNavigationItemSelected(-1))

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onFragmentBackStackChangedTest() {
        val presenter = MainActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            appContext = context
        )
        presenter.onFragmentBackStackChanged(stackIsClear = false, intent = Intent())

        Mockito.verify(mockView).updateNavigationViewsContent()

        Mockito.clearInvocations(mockView)
        presenter.onFragmentBackStackChanged(stackIsClear = true, intent = Intent())

        Mockito.verify(mockView).updateNavigationViewsContent()

        Mockito.clearInvocations(mockView)
        presenter.onFragmentBackStackChanged(
            stackIsClear = false,
            intent = Intent().putExtra(KEY_CONNECTION_ID, "connectionId1").putExtra(
                KEY_AUTHORIZATION_ID,
                "authorizationId1"
            )
        )

        Mockito.verify(mockView).updateNavigationViewsContent()

        Mockito.clearInvocations(mockView)
        presenter.onFragmentBackStackChanged(
            stackIsClear = true,
            intent = Intent().putExtra(KEY_CONNECTION_ID, "connectionId1").putExtra(
                KEY_AUTHORIZATION_ID,
                "authorizationId1"
            )
        )

        Mockito.verify(mockView).closeView()
    }

    @Test
    @Throws(Exception::class)
    fun onNavigationItemClickTest() {
        val presenter = MainActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            appContext = context
        )
        presenter.onNavigationItemClick(stackIsClear = false)

        Mockito.verify(mockView).popBackStack()

        Mockito.clearInvocations(mockView)
        presenter.onNavigationItemClick(stackIsClear = true)

        Mockito.verify(mockView).closeView()
    }

    private val mockView = Mockito.mock(MainActivityContract.View::class.java)
    private val mockConnectionsRepository = Mockito.mock(ConnectionsRepositoryAbs::class.java)
    private val context: Context = ApplicationProvider.getApplicationContext()
}
