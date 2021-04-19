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
package com.saltedge.authenticator.features.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_CLOSE_APP
import com.saltedge.authenticator.app.KEY_DEEP_LINK
import com.saltedge.authenticator.app.QR_SCAN_REQUEST_CODE
import com.saltedge.authenticator.interfaces.MenuItem
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.realm.RealmManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.constants.*
import com.saltedge.authenticator.sdk.api.model.appLink.ActionAppLinkData
import com.saltedge.authenticator.sdk.api.model.appLink.ConnectAppLinkData
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationIdentifier
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivityViewModelTest {

    private val mockRealmManager = mock(RealmManagerAbs::class.java)
    private val mockPreferenceRepository = mock(PreferenceRepositoryAbs::class.java)
    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun createViewModel(): MainActivityViewModel {
        return MainActivityViewModel(
            appContext = context,
            realmManager = mockRealmManager,
            preferenceRepository = mockPreferenceRepository,
            connectionsRepository = mockConnectionsRepository
        )
    }

    @Test
    @Throws(Exception::class)
    fun initTestCase1() {
        //given
        given(mockRealmManager.initialized).willReturn(true)

        //when
        val viewModel = createViewModel()

        //then
        Mockito.never()

    }

    @Test
    @Throws(Exception::class)
    fun initTestCase2() {
        //given
        given(mockRealmManager.initialized).willReturn(false)

        //when
        createViewModel()

        //then
        verify(mockRealmManager).initRealm(context)
    }

    @Test
    @Throws(Exception::class)
    fun onLifeCycleCreateTestCase1() {
        /**
         * given null savedInstanceState, null intent, no connections
         */
        val viewModel = createViewModel()
        val savedInstanceState: Bundle? = null
        val intent: Intent? = null
        given(mockConnectionsRepository.isEmpty()).willReturn(true)

        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, `is`(nullValue()))

        //when
        viewModel.onLifeCycleCreate(savedInstanceState, intent)

        //then onShowAuthorizationsListEvent is posted
        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowConnectEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowSubmitActionEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onLifeCycleCreateTestCase2() {
        /**
         * given null savedInstanceState, empty intent, no empty repository
         */
        val viewModel = createViewModel()
        val savedInstanceState: Bundle? = null
        val intent: Intent? = Intent()
        given(mockConnectionsRepository.isEmpty()).willReturn(false)

        //when
        viewModel.onLifeCycleCreate(savedInstanceState, intent)

        //then onShowAuthorizationsListEvent only is posted
        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowConnectEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowSubmitActionEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onLifeCycleCreateTestCase3() {
        /**
         * given null savedInstanceState, intent with Pending Authorization Data
         */
        val viewModel = createViewModel()
        val savedInstanceState: Bundle? = null
        val intent: Intent? =
            Intent().putExtra(KEY_CONNECTION_ID, "1").putExtra(KEY_AUTHORIZATION_ID, "2")
        given(mockConnectionsRepository.isEmpty()).willReturn(false)

        //when
        viewModel.onLifeCycleCreate(savedInstanceState, intent)

        //then onShowAuthorizationDetailsEvent is posted
        val bundle = viewModel.onShowAuthorizationDetailsEvent.value?.peekContent()

        assertThat(bundle?.getBoolean(KEY_CLOSE_APP), equalTo(true))
        assertThat(
            bundle?.getSerializable(KEY_ID) as AuthorizationIdentifier,
            equalTo(AuthorizationIdentifier(authorizationID = "2", connectionID = "1"))
        )
        assertThat(viewModel.onShowConnectEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowSubmitActionEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onLifeCycleCreateTestCase4() {
        /**
         * given null savedInstanceState, intent with Deep-link Data for Connection creation
         */
        val viewModel = createViewModel()
        val savedInstanceState: Bundle? = null
        val intent: Intent? = Intent().putExtra(
            KEY_DEEP_LINK,
            "authenticator://saltedge.com/connect?configuration=https://saltedge.com/configuration&connect_query=1234567890"
        )
        given(mockConnectionsRepository.isEmpty()).willReturn(false)

        //when
        viewModel.onLifeCycleCreate(savedInstanceState, intent)

        //then onShowConnectEvent is posted
        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, `is`(nullValue()))

        val bundle = viewModel.onShowConnectEvent.value?.peekContent()

        assertThat(
            bundle?.getSerializable(KEY_DATA) as ConnectAppLinkData,
            equalTo(
                ConnectAppLinkData(
                    configurationUrl = "https://saltedge.com/configuration",
                    connectQuery = "1234567890"
                )
            )
        )
        assertThat(viewModel.onShowSubmitActionEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onLifeCycleCreateTestCase5() {
        /**
         * given null savedInstanceState, intent with Deep-link Data for Instant Action
         */
        val viewModel = createViewModel()
        val savedInstanceState: Bundle? = null
        val intent: Intent? = Intent().putExtra(
            KEY_DEEP_LINK,
            "authenticator://saltedge.com/action?action_uuid=123456&return_to=https://return.com&connect_url=https://someurl.com"
        )
        given(mockConnectionsRepository.isEmpty()).willReturn(false)

        //when
        viewModel.onLifeCycleCreate(savedInstanceState, intent)

        //then onShowSubmitActionEvent is posted
        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowConnectEvent.value, `is`(nullValue()))

        val bundle = viewModel.onShowSubmitActionEvent.value?.peekContent()

        assertThat(
            bundle?.getSerializable(KEY_DATA) as ActionAppLinkData,
            equalTo(
                ActionAppLinkData(
                    actionUUID = "123456",
                    connectUrl = "https://someurl.com",
                    returnTo = "https://return.com"
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun onLifeCycleCreateTestCase6() {
        /**
         * given not null savedInstanceState
         */
        val viewModel = createViewModel()
        val savedInstanceState: Bundle? = Bundle()
        val intent: Intent? = Intent().putExtra(KEY_DEEP_LINK, "authenticator://saltedge.com")
        given(mockConnectionsRepository.isEmpty()).willReturn(false)

        //when
        viewModel.onLifeCycleCreate(savedInstanceState, intent)

        //then no interactions with observable values
        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowConnectEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowSubmitActionEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultTestCase1() {
        /**
         * given unknown requestCode
         */
        val viewModel = createViewModel()
        val requestCode = 0
        val resultCode: Int = Activity.RESULT_OK
        val intent: Intent? = Intent().putExtra(KEY_DEEP_LINK, "authenticator://saltedge.com")

        //when
        viewModel.onActivityResult(requestCode, resultCode, intent)

        //then no interactions with observable values
        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowConnectEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowSubmitActionEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultTestCase2() {
        /**
         * given RESULT_CANCELED resultCode
         */
        val viewModel = createViewModel()
        val requestCode = QR_SCAN_REQUEST_CODE
        val resultCode: Int = Activity.RESULT_CANCELED
        val intent: Intent? = Intent().putExtra(KEY_DEEP_LINK, "authenticator://saltedge.com")

        //when
        viewModel.onActivityResult(requestCode, resultCode, intent)

        //then no interactions with observable values
        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowConnectEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowSubmitActionEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultTestCase3() {
        /**
         * given null intent
         */
        val viewModel = createViewModel()
        val requestCode = QR_SCAN_REQUEST_CODE
        val resultCode: Int = Activity.RESULT_OK
        val intent: Intent? = null

        //when
        viewModel.onActivityResult(requestCode, resultCode, intent)

        //then no interactions with observable values
        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowConnectEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowSubmitActionEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultTestCase4() {
        /**
         * given QR_SCAN_REQUEST_CODE requestCode, RESULT_OK resultCode
         * and Intent with Deep-link Data for Connection creation
         */
        val viewModel = createViewModel()
        val requestCode = QR_SCAN_REQUEST_CODE
        val resultCode: Int = Activity.RESULT_OK
        val intent: Intent? = Intent().putExtra(
            KEY_DEEP_LINK,
            "authenticator://saltedge.com/connect?configuration=https://example.com/configuration&connect_query=1234567890"
        )

        //when
        viewModel.onActivityResult(requestCode, resultCode, intent)

        //then onShowConnectEvent is posted
        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, `is`(nullValue()))
        assertNotNull(viewModel.onShowConnectEvent.value)

        val bundle = viewModel.onShowConnectEvent.value?.peekContent()

        assertThat(
            bundle?.getSerializable(KEY_DATA) as ConnectAppLinkData,
            equalTo(
                ConnectAppLinkData(
                    configurationUrl = "https://example.com/configuration",
                    connectQuery = "1234567890"
                )
            )
        )
        assertThat(viewModel.onShowSubmitActionEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultTestCase5() {
        /**
         * given QR_SCAN_REQUEST_CODE requestCode, RESULT_OK resultCode
         * and Intent with Deep-link Data for Instant Action
         */
        val viewModel = createViewModel()
        val requestCode = QR_SCAN_REQUEST_CODE
        val resultCode: Int = Activity.RESULT_OK
        val intent: Intent? = Intent().putExtra(
            KEY_DEEP_LINK,
            "authenticator://saltedge.com/action?action_uuid=123456&return_to=https://return.com&connect_url=https://someurl.com"
        )

        //when
        viewModel.onActivityResult(requestCode, resultCode, intent)

        //then onShowSubmitActionEvent is posted
        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowConnectEvent.value, `is`(nullValue()))
        assertNotNull(viewModel.onShowSubmitActionEvent.value)

        val bundle = viewModel.onShowSubmitActionEvent.value?.peekContent()

        assertThat(
            bundle?.getSerializable(KEY_DATA) as ActionAppLinkData,
            equalTo(
                ActionAppLinkData(
                    actionUUID = "123456",
                    connectUrl = "https://someurl.com",
                    returnTo = "https://return.com"
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase1() {
        /**
         * given viewId = appBarActionQrCode
         */
        val viewModel = createViewModel()
        val viewId = R.id.appBarActionQrCode

        //when
        viewModel.onViewClick(viewId)

        //then
        assertThat(
            viewModel.onAppbarMenuItemClickEvent.value,
            equalTo(ViewModelEvent(MenuItem.SCAN_QR))
        )
        assertThat(viewModel.onBackActionClickEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase2() {
        /**
         * given viewId = appBarActionMore
         */
        val viewModel = createViewModel()
        val viewId = R.id.appBarActionMore

        //when
        viewModel.onViewClick(viewId)

        //then
        assertThat(
            viewModel.onAppbarMenuItemClickEvent.value,
            equalTo(ViewModelEvent(MenuItem.MORE_MENU))
        )
        assertThat(viewModel.onBackActionClickEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase3() {
        /**
         * given viewId = appBarBackAction
         */
        val viewModel = createViewModel()
        val viewId = R.id.appBarBackAction

        //when
        viewModel.onViewClick(viewId)

        //then onBackActionClickEvent is posted
        assertThat(viewModel.onAppbarMenuItemClickEvent.value, `is`(nullValue()))
        assertThat(viewModel.onBackActionClickEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase4() {
        /**
         * given invalid viewId
         */
        val viewModel = createViewModel()
        val viewId = R.id.activityRootLayout

        //when
        viewModel.onViewClick(viewId)

        //then no interactions with observable values
        assertThat(viewModel.onAppbarMenuItemClickEvent.value, `is`(nullValue()))
        assertThat(viewModel.onBackActionClickEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase5() {
        /**
         * given viewId = appBarActionSwitchTheme
         */
        val viewModel = createViewModel()
        val viewId = R.id.appBarActionSwitchTheme

        //when
        viewModel.onViewClick(viewId)

        //then
        assertThat(
            viewModel.onAppbarMenuItemClickEvent.value,
            equalTo(ViewModelEvent(MenuItem.CUSTOM_NIGHT_MODE))
        )
        assertThat(viewModel.onBackActionClickEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onNewAuthorizationTest() {
        /**
         * given authorizationIdentifier
         */
        val viewModel = createViewModel()
        val authorizationIdentifier =
            AuthorizationIdentifier(connectionID = "1", authorizationID = "2")

        //when
        viewModel.onNewAuthorization(authorizationIdentifier)

        //then onShowAuthorizationDetailsEvent is posted
        val bundle = viewModel.onShowActionAuthorizationEvent.value?.peekContent()
        assertThat(bundle?.getBoolean(KEY_CLOSE_APP), equalTo(true))
        assertThat(bundle?.getSerializable(KEY_ID), equalTo(authorizationIdentifier ?: ""))
        assertThat(bundle?.getInt(KEY_TITLE), equalTo(R.string.action_new_action_title))
    }

    @Test
    @Throws(Exception::class)
    fun updateAppbarTestCase1() {
        /**
         * given appbar params
         */
        val viewModel = createViewModel()
        val titleResId = R.string.app_name
        val title = null
        val backActionImageResId = null
        val showMenu = arrayOf(MenuItem.SCAN_QR, MenuItem.MORE_MENU)

        assertThat(
            viewModel.appBarBackActionImageResource.value,
            equalTo(R.drawable.ic_appbar_action_back)
        )

        //when
        viewModel.updateAppbar(titleResId, title, backActionImageResId, showMenu = showMenu)

        //then updated view
        assertThat(viewModel.appBarTitle.value, equalTo(context.getString(R.string.app_name)))
        assertThat(
            viewModel.appBarBackActionImageResource.value,
            equalTo(R.drawable.ic_appbar_action_back)
        )
        assertThat(viewModel.appBarBackActionVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.appBarActionQRVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.appBarActionMoreVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.appBarActionThemeVisibility.value, equalTo(View.GONE))
    }

    @Test
    @Throws(Exception::class)
    fun updateAppbarTestCase2() {
        /**
         * given appbar params
         */
        val viewModel = createViewModel()
        val titleResId = null
        val title = "Test"
        val actionImageResId = R.drawable.ic_appbar_action_close
        val showMenu = emptyArray<MenuItem>()

        //when
        viewModel.updateAppbar(titleResId, title, actionImageResId, showMenu)

        //then updated view
        assertThat(viewModel.appBarTitle.value, equalTo("Test"))
        assertThat(
            viewModel.appBarBackActionImageResource.value,
            equalTo(R.drawable.ic_appbar_action_close)
        )
        assertThat(viewModel.appBarBackActionVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.appBarActionQRVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.appBarActionMoreVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.appBarActionThemeVisibility.value, equalTo(View.GONE))
    }

    @Test
    @Throws(Exception::class)
    fun onLanguageChangedTest() {
        //given
        val viewModel = createViewModel()

        //when
        viewModel.onLanguageChanged()

        //then onRestartActivityEvent is posted
        assertThat(viewModel.onRestartActivityEvent.value, equalTo(ViewModelEvent(Unit)))
    }
}
