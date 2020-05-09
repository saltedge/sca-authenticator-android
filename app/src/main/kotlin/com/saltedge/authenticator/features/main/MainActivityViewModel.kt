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
import androidx.lifecycle.*
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.QR_SCAN_REQUEST_CODE
import com.saltedge.authenticator.features.actions.NewAuthorizationListener
import com.saltedge.authenticator.features.menu.MenuItemData
import com.saltedge.authenticator.interfaces.ActivityComponentsContract
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.realm.RealmManagerAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.model.appLink.ActionAppLinkData
import com.saltedge.authenticator.sdk.model.appLink.ConnectAppLinkData
import com.saltedge.authenticator.sdk.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.sdk.tools.extractActionAppLinkData
import com.saltedge.authenticator.sdk.tools.extractConnectAppLinkData
import com.saltedge.authenticator.tools.PasscodeToolsAbs
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.applyPreferenceLocale

class MainActivityViewModel(
    val appContext: Context,
    val preferenceRepository: PreferenceRepositoryAbs,
    val passcodeTools: PasscodeToolsAbs,
    val realmManager: RealmManagerAbs
) : ViewModel(),
    LifecycleObserver,
    NetworkStateChangeListener,
    NewAuthorizationListener,
    ActivityComponentsContract
{
    private val connectivityReceiver = ConnectivityReceiver()//TODO make ext dependency

    val onQrScanClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onAppBarMenuClickEvent = MutableLiveData<ViewModelEvent<List<MenuItemData>>>()
    val onBackActionClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onRestartActivityEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onShowAuthorizationsListEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onShowAuthorizationDetailsEvent = MutableLiveData<ViewModelEvent<AuthorizationIdentifier>>()
    val onShowConnectionsListEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onShowSettingsListEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onShowConsentsListEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onShowConnectEvent = MutableLiveData<ViewModelEvent<ConnectAppLinkData>>()
    val onShowSubmitActionEvent = MutableLiveData<ViewModelEvent<ActionAppLinkData>>()

    val internetConnectionWarningVisibility = MutableLiveData<Int>()
    val appBarTitle = MutableLiveData<String>()
    val appBarBackActionImageResource = MutableLiveData<ResId>(R.drawable.ic_appbar_action_back)
    val appBarBackActionVisibility = MutableLiveData<Int>(View.GONE)
    val appBarMenuVisibility = MutableLiveData<Int>()

    init {
        if (!realmManager.initialized) realmManager.initRealm(context = appContext)
    }

    fun onLifeCycleCreate(savedInstanceState: Bundle?, intent: Intent?) {
        if (savedInstanceState == null) {
            if (intent != null && (intent.hasPendingAuthorizationData || intent.hasDeepLinkData)) {
                onNewIntent(intent)
            } else {
                onShowAuthorizationsListEvent.postValue(ViewModelEvent(Unit))
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onLifeCycleResume() {
        connectivityReceiver.register(appContext, this)
        appContext.applyPreferenceLocale()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onLifeCyclePause() {
        connectivityReceiver.unregister(appContext)
    }

    /**
     * Handle Network connection changes
     */
    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        internetConnectionWarningVisibility.postValue(if (isConnected) View.GONE else View.VISIBLE)
    }

    /**
     * Handle result from QR Scanner
     * if result is correct then show Connect View
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == QR_SCAN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let { onNewIntent(intent = it) }
        }
    }

    /**
     * Handle Intents from external sources
     */
    fun onNewIntent(intent: Intent?) {
        when {
            intent.hasPendingAuthorizationData -> {
                onShowAuthorizationDetailsEvent.postValue(
                    ViewModelEvent(AuthorizationIdentifier(
                        authorizationID = intent.authorizationId,
                        connectionID = intent.connectionId
                    ))
                )
            }
            intent.hasDeepLinkData -> {
                intent.deepLink.extractConnectAppLinkData()?.let { connectionAppLinkData ->
                    onShowConnectEvent.postValue(ViewModelEvent(connectionAppLinkData))
                } ?: intent.deepLink.extractActionAppLinkData()?.let { actionAppLinkData ->
                    onShowSubmitActionEvent.postValue(ViewModelEvent(actionAppLinkData))
                }
            }
        }
    }

    /**
     * Handle click on views
     */
    fun onViewClick(viewId: Int) {
        when (viewId) {
            R.id.appBarActionQrCode -> onQrScanClickEvent.postValue(ViewModelEvent(Unit))
            R.id.appBarActionMenu -> {
                val menuItems = listOf<MenuItemData>(
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
                onAppBarMenuClickEvent.postValue(ViewModelEvent(menuItems))
            }
            R.id.appBarBackAction -> onBackActionClickEvent.postValue(ViewModelEvent(Unit))
        }
    }

    /**
     * Handle clicks on menu
     */
    fun onMenuItemSelected(menuId: String, selectedItemId: Int) {
        when (selectedItemId) {
            R.string.connections_feature_title -> onShowConnectionsListEvent.postValue(ViewModelEvent(Unit))
            R.string.consents_feature_title -> onShowConsentsListEvent.postValue(ViewModelEvent(Unit))
            R.string.settings_feature_title -> onShowSettingsListEvent.postValue(ViewModelEvent(Unit))
        }
    }

    /**
     * Handle new authorization event (e.g. from ActionSubmit)
     */
    override fun onNewAuthorization(authorizationIdentifier: AuthorizationIdentifier) {
        onShowAuthorizationDetailsEvent.postValue(ViewModelEvent(authorizationIdentifier))
    }

    /**
     * Handle app bar events
     */
    override fun updateAppbar(
        titleResId: ResId?,
        title: String?,
        backActionImageResId: ResId?,
        showMenu: Boolean
    ) {
        appBarTitle.postValue(titleResId?.let { appContext.getString(it) } ?: title ?: "")
        backActionImageResId?.let { appBarBackActionImageResource.postValue(it) }
        appBarBackActionVisibility.postValue(if (backActionImageResId == null) View.GONE else View.VISIBLE)
        appBarMenuVisibility.postValue(if (showMenu) View.VISIBLE else View.GONE)
    }

    override fun onLanguageChanged() {
        appContext.applyPreferenceLocale()
        onRestartActivityEvent.postValue(ViewModelEvent(Unit))
    }
}
