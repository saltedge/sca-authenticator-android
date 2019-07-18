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
package com.saltedge.authenticator.unitTests.features.main

import android.app.Activity
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_CONNECT_CONFIGURATION
import com.saltedge.authenticator.app.QR_SCAN_REQUEST_CODE
import com.saltedge.authenticator.features.main.MainActivityContract
import com.saltedge.authenticator.features.main.MainActivityPresenter
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.constants.KEY_AUTHORIZATION_ID
import com.saltedge.authenticator.sdk.constants.KEY_CONNECTION_ID
import com.saltedge.authenticator.sdk.model.ProviderData
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class MainActivityPresenterTest {

    @Test
    @Throws(Exception::class)
    fun getNavigationIconResourceIdTest() {
        val presenter = MainActivityPresenter(viewContract = mockView, connectionsRepository = mockConnectionsRepository)

        Assert.assertNotNull(presenter.viewContract)
        assertThat(presenter.getNavigationIcon(isTopNavigationLevel = false),
                equalTo(R.drawable.ic_arrow_back_white_24dp))
        Assert.assertNull(presenter.getNavigationIcon(isTopNavigationLevel = true))
    }

    @Test
    @Throws(Exception::class)
    fun launchInitialFragmentTest_normalMode() {
        val presenter = MainActivityPresenter(viewContract = mockView, connectionsRepository = mockConnectionsRepository)
        presenter.setInitialData(Intent())

        Mockito.doReturn(false).`when`(mockConnectionsRepository).hasActiveConnections()
        presenter.launchInitialFragment()

        Mockito.verify(mockView).setSelectedTabbarItemId(R.id.menu_connections)

        Mockito.doReturn(true).`when`(mockConnectionsRepository).hasActiveConnections()
        Mockito.clearInvocations(mockConnectionsRepository)
        presenter.launchInitialFragment()

        Mockito.verify(mockView).setSelectedTabbarItemId(R.id.menu_authorizations)
    }

    @Test
    @Throws(Exception::class)
    fun launchInitialFragmentTest_quickConfirmMode() {
        val presenter = MainActivityPresenter(viewContract = mockView, connectionsRepository = mockConnectionsRepository)
        presenter.setInitialData(Intent().putExtra(KEY_CONNECTION_ID, "connectionId1").putExtra(KEY_AUTHORIZATION_ID, "authorizationId1"))

        presenter.launchInitialFragment()

        Mockito.verify(mockView).hideNavigationBar()
        Mockito.verify(mockView).showAuthorizationDetailsView("connectionId1", "authorizationId1", true)
    }

    @Test
    @Throws(Exception::class)
    fun onNavigationItemSelectedTest() {
        val presenter = MainActivityPresenter(viewContract = mockView, connectionsRepository = mockConnectionsRepository)

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
    fun onActivityResultTest() {
        val presenter = MainActivityPresenter(viewContract = mockView, connectionsRepository = mockConnectionsRepository)
        val intent = Intent().putExtra(KEY_CONNECT_CONFIGURATION, "configuration_link")
        presenter.onActivityResult(requestCode = QR_SCAN_REQUEST_CODE, resultCode = Activity.RESULT_OK, data = intent)

        Mockito.verify(mockView).showConnectProvider(connectConfigurationLink = "configuration_link")

        Mockito.clearInvocations(mockView)
        presenter.onActivityResult(requestCode = 0, resultCode = Activity.RESULT_OK, data = intent)

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.onActivityResult(requestCode = 0, resultCode = Activity.RESULT_CANCELED, data = intent)

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.onActivityResult(requestCode = 0, resultCode = Activity.RESULT_CANCELED, data = null)

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onFragmentBackStackChangedTest() {
        val presenter = MainActivityPresenter(viewContract = mockView, connectionsRepository = mockConnectionsRepository)
        presenter.setInitialData(Intent())
        presenter.onFragmentBackStackChanged(stackIsClear = false)

        Mockito.verify(mockView).updateNavigationViewsContent()

        Mockito.clearInvocations(mockView)
        presenter.setInitialData(Intent())
        presenter.onFragmentBackStackChanged(stackIsClear = true)

        Mockito.verify(mockView).updateNavigationViewsContent()

        Mockito.clearInvocations(mockView)
        presenter.setInitialData(Intent().putExtra(KEY_CONNECTION_ID, "connectionId1").putExtra(KEY_AUTHORIZATION_ID, "authorizationId1"))
        presenter.onFragmentBackStackChanged(stackIsClear = false)

        Mockito.verify(mockView).updateNavigationViewsContent()

        Mockito.clearInvocations(mockView)
        presenter.setInitialData(Intent().putExtra(KEY_CONNECTION_ID, "connectionId1").putExtra(KEY_AUTHORIZATION_ID, "authorizationId1"))
        presenter.onFragmentBackStackChanged(stackIsClear = true)

        Mockito.verify(mockView).closeView()
    }

    @Test
    @Throws(Exception::class)
    fun onNavigationItemClickTest() {
        val presenter = MainActivityPresenter(viewContract = mockView, connectionsRepository = mockConnectionsRepository)
        presenter.onNavigationItemClick(stackIsClear = false)

        Mockito.verify(mockView).popBackStack()

        Mockito.clearInvocations(mockView)
        presenter.onNavigationItemClick(stackIsClear = true)

        Mockito.verify(mockView).closeView()
    }

    private val mockView = Mockito.mock(MainActivityContract.View::class.java)
    private val mockConnectionsRepository = Mockito.mock(ConnectionsRepositoryAbs::class.java)
}
