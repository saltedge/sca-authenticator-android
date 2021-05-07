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
import com.saltedge.authenticator.app.KEY_CLOSE_APP
import com.saltedge.authenticator.app.QR_SCAN_REQUEST_CODE
import com.saltedge.authenticator.features.actions.NewAuthorizationListener
import com.saltedge.authenticator.interfaces.ActivityComponentsContract
import com.saltedge.authenticator.interfaces.MenuItem
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.realm.RealmManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.sdk.constants.KEY_DATA
import com.saltedge.authenticator.sdk.constants.KEY_ID
import com.saltedge.authenticator.sdk.constants.KEY_TITLE
import com.saltedge.authenticator.sdk.tools.extractActionAppLinkData
import com.saltedge.authenticator.sdk.v2.tools.extractConnectAppLinkDataV2
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.applyPreferenceLocale
import com.saltedge.authenticator.tools.postUnitEvent

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
    val onAppbarMenuItemClickEvent = MutableLiveData<ViewModelEvent<MenuItem>>()
    val onBackActionClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onRestartActivityEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onShowAuthorizationDetailsEvent = MutableLiveData<ViewModelEvent<Bundle>>()
    val onShowActionAuthorizationEvent = MutableLiveData<ViewModelEvent<Bundle>>()
    val onShowConnectEvent = MutableLiveData<ViewModelEvent<Bundle>>()
    val onShowSubmitActionEvent = MutableLiveData<ViewModelEvent<Bundle>>()
    val onQrScanClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
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
            intent.hasPendingAuthorizationData -> {//Data from push notification
                val authorizationIdentifier = AuthorizationIdentifier(
                    authorizationID = intent.authorizationId,
                    connectionID = intent.connectionId
                )
                onShowAuthorizationDetailsEvent.postValue(
                    ViewModelEvent(Bundle().apply {
                        putSerializable(KEY_ID, authorizationIdentifier)
                        putBoolean(KEY_CLOSE_APP, true)
                    })
                )
            }
            intent.hasDeepLinkData -> {
                initialQrScanWasStarted = true
                val connectionAppLinkData = intent.deepLink.extractConnectAppLinkDataV2()
                val actionAppLinkData = intent.deepLink.extractActionAppLinkData()
                if (connectionAppLinkData != null) {
                    onShowConnectEvent.postValue(ViewModelEvent(Bundle().apply {
                        putSerializable(KEY_DATA, connectionAppLinkData)
                    }))
                } else if (actionAppLinkData != null) {
                    onShowSubmitActionEvent.postValue(ViewModelEvent(Bundle().apply {
                        putSerializable(KEY_DATA, actionAppLinkData)
                    }))
                }
            }
        }
    }

    /**
     * Handle click on appbar actions
     */
    fun onViewClick(viewId: Int) {
        when (viewId) {
            R.id.appBarActionQrCode -> onAppbarMenuItemClickEvent.postValue(ViewModelEvent(MenuItem.SCAN_QR))
            R.id.appBarActionSwitchTheme -> onAppbarMenuItemClickEvent.postValue(ViewModelEvent(MenuItem.CUSTOM_NIGHT_MODE))
            R.id.appBarActionMore -> onAppbarMenuItemClickEvent.postValue(ViewModelEvent(MenuItem.MORE_MENU))
            R.id.appBarBackAction -> onBackActionClickEvent.postUnitEvent()
        }
    }

    fun onUnlock() {
        if (!initialQrScanWasStarted && connectionsRepository.isEmpty()) {
            onQrScanClickEvent.postUnitEvent()
            initialQrScanWasStarted = true
        }
    }

    /**
     * Handle new authorization event (e.g. from ActionSubmit)
     */
    override fun onNewAuthorization(authorizationIdentifier: AuthorizationIdentifier) {
        onShowActionAuthorizationEvent.postValue(ViewModelEvent(Bundle().apply {
            putSerializable(KEY_ID, authorizationIdentifier)
            putBoolean(KEY_CLOSE_APP, true)
            putInt(KEY_TITLE, R.string.action_new_action_title)
        }))
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
        appBarActionThemeVisibility.postValue(if (showMenu.contains(MenuItem.CUSTOM_NIGHT_MODE)) View.VISIBLE else View.GONE)
        appBarActionMoreVisibility.postValue(if (showMenu.contains(MenuItem.MORE_MENU)) View.VISIBLE else View.GONE)
    }

    override fun onLanguageChanged() {
        appContext.applyPreferenceLocale()
        onRestartActivityEvent.postUnitEvent()
    }
}
