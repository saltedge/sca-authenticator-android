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
import com.saltedge.authenticator.app.switchDarkLightMode
import com.saltedge.authenticator.features.actions.NewAuthorizationListener
import com.saltedge.authenticator.features.menu.MenuItemData
import com.saltedge.authenticator.interfaces.ActivityComponentsContract
import com.saltedge.authenticator.interfaces.MenuItem
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.realm.RealmManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.model.appLink.ActionAppLinkData
import com.saltedge.authenticator.sdk.model.appLink.ConnectAppLinkData
import com.saltedge.authenticator.sdk.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.sdk.tools.extractActionAppLinkData
import com.saltedge.authenticator.sdk.tools.extractConnectAppLinkData
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.applyPreferenceLocale
import com.saltedge.authenticator.tools.postEvent

class MainActivityViewModel(
    val appContext: Context,
    val realmManager: RealmManagerAbs,
    val preferenceRepository: PreferenceRepositoryAbs,
    val connectionsRepository: ConnectionsRepositoryAbs
) : ViewModel(),
    LifecycleObserver,
    NewAuthorizationListener,
    ActivityComponentsContract
{
    val onQrScanClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onAppBarMenuClickEvent = MutableLiveData<ViewModelEvent<List<MenuItemData>>>()
    val onBackActionClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onRestartActivityEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onShowAuthorizationsListEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onShowAuthorizationDetailsEvent = MutableLiveData<ViewModelEvent<AuthorizationIdentifier>>()
    val onShowConnectionsListEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onShowSettingsListEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onShowConnectEvent = MutableLiveData<ViewModelEvent<ConnectAppLinkData>>()
    val onShowSubmitActionEvent = MutableLiveData<ViewModelEvent<ActionAppLinkData>>()

    val appBarTitle = MutableLiveData<String>()
    val appBarBackActionImageResource = MutableLiveData<ResId>(R.drawable.ic_appbar_action_back)
    val appBarBackActionVisibility = MutableLiveData<Int>(View.GONE)
    val appBarActionQRVisibility = MutableLiveData<Int>(View.GONE)
    val appBarActionThemeVisibility = MutableLiveData<Int>(View.GONE)
    val appBarActionMoreVisibility = MutableLiveData<Int>(View.GONE)

    private var initialQrScanWasStarted = false

    init {
        if (!realmManager.initialized) realmManager.initRealm(context = appContext)
    }

    fun bindLifecycleObserver(lifecycle: Lifecycle) {
        lifecycle.let {
            it.removeObserver(this)
            it.addObserver(this)
        }
    }

    fun onLifeCycleCreate(savedInstanceState: Bundle?, intent: Intent?) {
        if (savedInstanceState == null) {
            onShowAuthorizationsListEvent.postValue(ViewModelEvent(Unit))
            if (intent != null && (intent.hasPendingAuthorizationData || intent.hasDeepLinkData)) {
                onNewIntent(intent)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onLifeCycleResume() {
        appContext.applyPreferenceLocale()
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
            R.id.appBarActionTheme -> {
                val nightMode = preferenceRepository.nightMode
                preferenceRepository.nightMode = appContext.switchDarkLightMode(nightMode)
            }
            R.id.appBarActionMore -> {
                val menuItems = listOf<MenuItemData>(
                    MenuItemData(
                        id = R.string.connections_feature_title,
                        iconResId = R.drawable.ic_menu_action_connections,
                        textResId = R.string.connections_feature_title
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
            R.string.settings_feature_title -> onShowSettingsListEvent.postValue(ViewModelEvent(Unit))
        }
    }

    fun onUnlock() {
        if (!initialQrScanWasStarted && connectionsRepository.isEmpty()) {
            onQrScanClickEvent.postEvent()
            initialQrScanWasStarted = true
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
        showMenu: Array<MenuItem>
    ) {
        appBarTitle.postValue(titleResId?.let { appContext.getString(it) } ?: title ?: "")
        backActionImageResId?.let { appBarBackActionImageResource.postValue(it) }
        appBarBackActionVisibility.postValue(if (backActionImageResId == null) View.GONE else View.VISIBLE)
        appBarActionQRVisibility.postValue(if (showMenu.contains(MenuItem.SCAN_QR)) View.VISIBLE else View.GONE)
        appBarActionThemeVisibility.postValue(if (showMenu.contains(MenuItem.THEME)) View.VISIBLE else View.GONE)
        appBarActionMoreVisibility.postValue(if (showMenu.contains(MenuItem.MORE)) View.VISIBLE else View.GONE)
    }

    override fun onLanguageChanged() {
        appContext.applyPreferenceLocale()
        onRestartActivityEvent.postValue(ViewModelEvent(Unit))
    }
}
