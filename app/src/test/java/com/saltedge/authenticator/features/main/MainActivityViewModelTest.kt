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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ConnectivityReceiverAbs
import com.saltedge.authenticator.app.KEY_DEEP_LINK
import com.saltedge.authenticator.app.QR_SCAN_REQUEST_CODE
import com.saltedge.authenticator.features.menu.MenuItemData
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.realm.RealmManagerAbs
import com.saltedge.authenticator.sdk.constants.KEY_AUTHORIZATION_ID
import com.saltedge.authenticator.sdk.constants.KEY_CONNECTION_ID
import com.saltedge.authenticator.sdk.model.appLink.ActionAppLinkData
import com.saltedge.authenticator.sdk.model.appLink.ConnectAppLinkData
import com.saltedge.authenticator.sdk.model.authorization.AuthorizationIdentifier
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivityViewModelTest {

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
        Mockito.verify(mockRealmManager).initRealm(context)
    }

    @Test
    @Throws(Exception::class)
    fun onLifeCycleCreateTestCase1() {
        /**
         * given null savedInstanceState, null intent
         */
        val viewModel = createViewModel()
        val savedInstanceState: Bundle? = null
        val intent: Intent? = null
        assertThat(viewModel.onShowAuthorizationsListEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, `is`(nullValue()))

        //when
        viewModel.onLifeCycleCreate(savedInstanceState, intent)

        //then onShowAuthorizationsListEvent is posted
        assertThat(viewModel.onShowAuthorizationsListEvent.value, equalTo(ViewModelEvent(Unit)))
        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowConnectEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowSubmitActionEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onLifeCycleCreateTestCase2() {
        /**
         * given null savedInstanceState, empty intent
         */
        val viewModel = createViewModel()
        val savedInstanceState: Bundle? = null
        val intent: Intent? = Intent()

        //when
        viewModel.onLifeCycleCreate(savedInstanceState, intent)

        //then onShowAuthorizationsListEvent is posted
        assertThat(viewModel.onShowAuthorizationsListEvent.value, equalTo(ViewModelEvent(Unit)))
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
        val intent: Intent? = Intent().putExtra(KEY_CONNECTION_ID, "1").putExtra(KEY_AUTHORIZATION_ID, "2")

        //when
        viewModel.onLifeCycleCreate(savedInstanceState, intent)

        //then onShowAuthorizationDetailsEvent is posted
        assertThat(viewModel.onShowAuthorizationsListEvent.value, `is`(nullValue()))
        assertThat(
            viewModel.onShowAuthorizationDetailsEvent.value,
            equalTo(ViewModelEvent(AuthorizationIdentifier(authorizationID = "2", connectionID = "1")))
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
        val intent: Intent? = Intent().putExtra(KEY_DEEP_LINK, "authenticator://saltedge.com/connect?configuration=https://saltedge.com/configuration&connect_query=1234567890")

        //when
        viewModel.onLifeCycleCreate(savedInstanceState, intent)

        //then onShowAuthorizationsListEvent is posted
        assertThat(viewModel.onShowAuthorizationsListEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowConnectEvent.value,
            equalTo(ViewModelEvent(ConnectAppLinkData(
                configurationUrl = "https://saltedge.com/configuration",
                connectQuery = "1234567890"
            ))))
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
        val intent: Intent? = Intent().putExtra(KEY_DEEP_LINK, "authenticator://saltedge.com/action?action_uuid=123456&return_to=https://return.com&connect_url=https://someurl.com")

        //when
        viewModel.onLifeCycleCreate(savedInstanceState, intent)

        //then onShowAuthorizationsListEvent is posted
        assertThat(viewModel.onShowAuthorizationsListEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowConnectEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowSubmitActionEvent.value,
            equalTo(ViewModelEvent(ActionAppLinkData(
                actionUUID = "123456",
                connectUrl = "https://someurl.com",
                returnTo = "https://return.com"
            ))))
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

        //when
        viewModel.onLifeCycleCreate(savedInstanceState, intent)

        //then no interactions with observable values
        assertThat(viewModel.onShowAuthorizationsListEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowConnectEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowSubmitActionEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onLifeCycleResumeTest() {
        //given
        val lifecycle = LifecycleRegistry(mock(LifecycleOwner::class.java))
        val viewModel = createViewModel()
        viewModel.bindLifecycleObserver(lifecycle)

        //when
        lifecycle.currentState = Lifecycle.State.RESUMED

        //then
        Mockito.verify(mockConnectivityReceiver).addNetworkStateChangeListener(viewModel)
    }

    @Test
    @Throws(Exception::class)
    fun onLifeCyclePauseTest() {
        //given
        val lifecycle = LifecycleRegistry(mock(LifecycleOwner::class.java))
        val viewModel = createViewModel()
        viewModel.bindLifecycleObserver(lifecycle)

        //when
        lifecycle.currentState = Lifecycle.State.RESUMED
        lifecycle.currentState = Lifecycle.State.STARTED//move to pause state (possible only after RESUMED state)

        //then
        assertThat(viewModel.event, equalTo(Lifecycle.Event.ON_PAUSE))
        Mockito.verify(mockConnectivityReceiver).removeNetworkStateChangeListener(viewModel)
    }

    @Test
    @Throws(Exception::class)
    fun onNetworkConnectionChangedTestCase1() {
        //given
        val isConnected = false
        val viewModel = createViewModel()

        //when
        viewModel.onNetworkConnectionChanged(isConnected = isConnected)

        //then
        assertThat(viewModel.internetConnectionWarningVisibility.value, equalTo(View.VISIBLE))
    }

    @Test
    @Throws(Exception::class)
    fun onNetworkConnectionChangedTestCase2() {
        //given
        val isConnected = true
        val viewModel = createViewModel()

        //when
        viewModel.onNetworkConnectionChanged(isConnected = isConnected)

        //then
        assertThat(viewModel.internetConnectionWarningVisibility.value, equalTo(View.GONE))
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
        assertThat(viewModel.onShowAuthorizationsListEvent.value, `is`(nullValue()))
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
        assertThat(viewModel.onShowAuthorizationsListEvent.value, `is`(nullValue()))
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
        assertThat(viewModel.onShowAuthorizationsListEvent.value, `is`(nullValue()))
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
        val intent: Intent? = Intent().putExtra(KEY_DEEP_LINK, "authenticator://saltedge.com/connect?configuration=https://example.com/configuration&connect_query=1234567890")

        //when
        viewModel.onActivityResult(requestCode, resultCode, intent)

        //then onShowConnectEvent is posted
        assertThat(viewModel.onShowAuthorizationsListEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowConnectEvent.value,
            equalTo(ViewModelEvent(ConnectAppLinkData(
                configurationUrl = "https://example.com/configuration",
                connectQuery = "1234567890"
            ))))
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
        val intent: Intent? = Intent().putExtra(KEY_DEEP_LINK, "authenticator://saltedge.com/action?action_uuid=123456&return_to=https://return.com&connect_url=https://someurl.com")

        //when
        viewModel.onActivityResult(requestCode, resultCode, intent)

        //then onShowSubmitActionEvent is posted
        assertThat(viewModel.onShowAuthorizationsListEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowConnectEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowSubmitActionEvent.value,
            equalTo(ViewModelEvent(ActionAppLinkData(
                actionUUID = "123456",
                connectUrl = "https://someurl.com",
                returnTo = "https://return.com"
            ))))
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

        //then onQrScanClickEvent is posted
        assertThat(viewModel.onQrScanClickEvent.value, equalTo(ViewModelEvent(Unit)))
        assertThat(viewModel.onAppBarMenuClickEvent.value, `is`(nullValue()))
        assertThat(viewModel.onBackActionClickEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase2() {
        /**
         * given viewId = appBarActionMenu
         */
        val viewModel = createViewModel()
        val viewId = R.id.appBarActionMenu

        //when
        viewModel.onViewClick(viewId)

        //then onAppBarMenuClickEvent is posted
        assertThat(viewModel.onQrScanClickEvent.value, `is`(nullValue()))
        assertThat(viewModel.onAppBarMenuClickEvent.value, equalTo(ViewModelEvent(
            listOf<MenuItemData>(
                MenuItemData(
                    id = R.string.connections_feature_title,
                    iconResId = R.drawable.ic_menu_action_list,
                    textResId = R.string.connections_feature_title
                ),
                MenuItemData(
                    id = R.string.consents_feature_title,
                    iconResId = R.drawable.ic_menu_action_list,
                    textResId = R.string.consents_feature_title
                ),
                MenuItemData(
                    id = R.string.settings_feature_title,
                    iconResId = R.drawable.ic_menu_action_settings,
                    textResId = R.string.settings_feature_title
                )
            )
        )))
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
        assertThat(viewModel.onQrScanClickEvent.value, `is`(nullValue()))
        assertThat(viewModel.onAppBarMenuClickEvent.value, `is`(nullValue()))
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
        assertThat(viewModel.onQrScanClickEvent.value, `is`(nullValue()))
        assertThat(viewModel.onAppBarMenuClickEvent.value, `is`(nullValue()))
        assertThat(viewModel.onBackActionClickEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onMenuItemSelectedTestCase1() {
        /**
         * given selectedItemId = R.string.connections_feature_title
         */
        val viewModel = createViewModel()
        val selectedItemId = R.string.connections_feature_title

        //when
        viewModel.onMenuItemSelected("", selectedItemId)

        //then onShowConnectionsListEvent is posted
        assertThat(viewModel.onShowConnectionsListEvent.value, equalTo(ViewModelEvent(Unit)))
        assertThat(viewModel.onShowConsentsListEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowSettingsListEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onMenuItemSelectedTestCase2() {
        /**
         * given selectedItemId = R.string.consents_feature_title
         */
        val viewModel = createViewModel()
        val selectedItemId = R.string.consents_feature_title

        //when
        viewModel.onMenuItemSelected("", selectedItemId)

        //then onShowConsentsListEvent is posted
        assertThat(viewModel.onShowConnectionsListEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowConsentsListEvent.value, equalTo(ViewModelEvent(Unit)))
        assertThat(viewModel.onShowSettingsListEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onMenuItemSelectedTestCase3() {
        /**
         * given selectedItemId = R.string.settings_feature_title
         */
        val viewModel = createViewModel()
        val selectedItemId = R.string.settings_feature_title

        //when
        viewModel.onMenuItemSelected("", selectedItemId)

        //then onShowSettingsListEvent is posted
        assertThat(viewModel.onShowConnectionsListEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowConsentsListEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowSettingsListEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun onMenuItemSelectedTestCase4() {
        /**
         * given invalid viewId
         */
        val viewModel = createViewModel()
        val selectedItemId = R.string.app_name

        //when
        viewModel.onMenuItemSelected("", selectedItemId)

        //then no interactions with observable values
        assertThat(viewModel.onShowConnectionsListEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowConsentsListEvent.value, `is`(nullValue()))
        assertThat(viewModel.onShowSettingsListEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onNewAuthorizationTest() {
        /**
         * given authorizationIdentifier
         */
        val viewModel = createViewModel()
        val authorizationIdentifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "2")

        //when
        viewModel.onNewAuthorization(authorizationIdentifier)

        //then onShowAuthorizationDetailsEvent is posted
        assertThat(viewModel.onShowAuthorizationDetailsEvent.value, equalTo(ViewModelEvent(authorizationIdentifier)))
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
        val showMenu = true
        assertThat(viewModel.appBarBackActionImageResource.value, equalTo(R.drawable.ic_appbar_action_back))

        //when
        viewModel.updateAppbar(titleResId, title, backActionImageResId, showMenu)

        //then updated view
        assertThat(viewModel.appBarTitle.value, equalTo(context.getString(R.string.app_name)))
        assertThat(viewModel.appBarBackActionImageResource.value, equalTo(R.drawable.ic_appbar_action_back))
        assertThat(viewModel.appBarBackActionVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.appBarMenuVisibility.value, equalTo(View.VISIBLE))
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
        val showMenu = false

        //when
        viewModel.updateAppbar(titleResId, title, actionImageResId, showMenu)

        //then updated view
        assertThat(viewModel.appBarTitle.value, equalTo("Test"))
        assertThat(viewModel.appBarBackActionImageResource.value, equalTo(R.drawable.ic_appbar_action_close))
        assertThat(viewModel.appBarBackActionVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.appBarMenuVisibility.value, equalTo(View.GONE))
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

    private val mockRealmManager = Mockito.mock(RealmManagerAbs::class.java)
    private val mockConnectivityReceiver = Mockito.mock(ConnectivityReceiverAbs::class.java)
    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun createViewModel(): MainActivityViewModel {
        return MainActivityViewModel(
            appContext = context,
            realmManager = mockRealmManager,
            connectivityReceiver = mockConnectivityReceiver
        )
    }
}