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
package com.saltedge.authenticator.app

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.saltedge.authenticator.features.authorizations.details.AuthorizationDetailsViewModel
import com.saltedge.authenticator.features.authorizations.list.AuthorizationsListViewModel
import com.saltedge.authenticator.features.connections.create.ConnectProviderViewModel
import com.saltedge.authenticator.features.connections.list.ConnectionsListViewModel
import com.saltedge.authenticator.features.launcher.LauncherViewModel
import com.saltedge.authenticator.features.main.MainActivityViewModel
import com.saltedge.authenticator.features.onboarding.OnboardingSetupViewModel
import com.saltedge.authenticator.features.qr.QrScannerViewModel
import com.saltedge.authenticator.features.settings.about.AboutViewModel
import com.saltedge.authenticator.features.settings.language.LanguageSelectViewModel
import com.saltedge.authenticator.features.settings.licenses.LicensesViewModel
import com.saltedge.authenticator.features.settings.list.SettingsListViewModel
import com.saltedge.authenticator.features.settings.passcode.PasscodeEditViewModel
import com.saltedge.authenticator.models.realm.RealmManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.tools.biometric.BiometricToolsAbs
import com.saltedge.authenticator.sdk.tools.crypt.CryptoToolsAbs
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.tools.PasscodeToolsAbs
import javax.inject.Inject

class ViewModelsFactory @Inject constructor(
    val appContext: Context,
    val passcodeTools: PasscodeToolsAbs,
    val biometricTools: BiometricToolsAbs,
    val cryptoTools: CryptoToolsAbs,
    val preferenceRepository: PreferenceRepositoryAbs,
    val connectionsRepository: ConnectionsRepositoryAbs,
    val keyStoreManager: KeyStoreManagerAbs,
    val realmManager: RealmManagerAbs,
    val apiManager: AuthenticatorApiManagerAbs,
    val connectivityReceiver: ConnectivityReceiverAbs
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(LauncherViewModel::class.java) -> {
                return LauncherViewModel(
                    appContext = appContext,
                    preferenceRepository = preferenceRepository,
                    passcodeTools = passcodeTools,
                    realmManager = realmManager
                ) as T
            }
            modelClass.isAssignableFrom(MainActivityViewModel::class.java) -> {
                return MainActivityViewModel(
                    appContext = appContext,
                    realmManager = realmManager,
                    preferenceRepository = preferenceRepository
                ) as T
            }
            modelClass.isAssignableFrom(OnboardingSetupViewModel::class.java) -> {
                return OnboardingSetupViewModel(
                    appContext = appContext,
                    passcodeTools = passcodeTools,
                    preferenceRepository = preferenceRepository,
                    biometricTools = biometricTools
                ) as T
            }
            modelClass.isAssignableFrom(QrScannerViewModel::class.java) -> {
                return QrScannerViewModel(appContext = appContext) as T
            }
            modelClass.isAssignableFrom(AuthorizationsListViewModel::class.java) -> {
                return AuthorizationsListViewModel(
                    appContext = appContext,
                    connectionsRepository = connectionsRepository,
                    keyStoreManager = keyStoreManager,
                    cryptoTools = cryptoTools,
                    apiManager = apiManager,
                    connectivityReceiver = connectivityReceiver
                ) as T
            }
            modelClass.isAssignableFrom(AuthorizationDetailsViewModel::class.java) -> {
                return AuthorizationDetailsViewModel(
                    appContext = appContext,
                    connectionsRepository = connectionsRepository,
                    keyStoreManager = keyStoreManager,
                    cryptoTools = cryptoTools,
                    apiManager = apiManager
                ) as T
            }
            modelClass.isAssignableFrom(ConnectProviderViewModel::class.java) -> {
                return ConnectProviderViewModel(
                    appContext = appContext,
                    preferenceRepository = preferenceRepository,
                    connectionsRepository = connectionsRepository,
                    keyStoreManager = keyStoreManager,
                    apiManager = apiManager
                ) as T
            }
            modelClass.isAssignableFrom(ConnectionsListViewModel::class.java) -> {
                return ConnectionsListViewModel(
                    appContext = appContext,
                    connectionsRepository = connectionsRepository,
                    keyStoreManager = keyStoreManager,
                    apiManager = apiManager
                ) as T
            }
            modelClass.isAssignableFrom(SettingsListViewModel::class.java) -> {
                return SettingsListViewModel(
                    appContext = appContext,
                    preferenceRepository = preferenceRepository,
                    connectionsRepository = connectionsRepository,
                    keyStoreManager = keyStoreManager,
                    apiManager = apiManager
                ) as T
            }
            modelClass.isAssignableFrom(PasscodeEditViewModel::class.java) -> {
                return PasscodeEditViewModel(passcodeTools) as T
            }
            modelClass.isAssignableFrom(AboutViewModel::class.java) -> {
                return AboutViewModel(appContext) as T
            }
            modelClass.isAssignableFrom(LicensesViewModel::class.java) -> {
                return LicensesViewModel(appContext) as T
            }
            modelClass.isAssignableFrom(LanguageSelectViewModel::class.java) -> {
                return LanguageSelectViewModel(
                    appContext = appContext,
                    preferenceRepository = preferenceRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
