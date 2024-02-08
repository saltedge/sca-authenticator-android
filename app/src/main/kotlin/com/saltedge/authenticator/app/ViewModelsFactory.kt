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
import com.saltedge.authenticator.cloud.PushTokenUpdater
import com.saltedge.authenticator.core.tools.biometric.BiometricToolsAbs
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.actions.SubmitActionViewModel
import com.saltedge.authenticator.features.authorizations.details.AuthorizationDetailsInteractorV1
import com.saltedge.authenticator.features.authorizations.details.AuthorizationDetailsInteractorV2
import com.saltedge.authenticator.features.authorizations.details.AuthorizationDetailsViewModel
import com.saltedge.authenticator.features.authorizations.list.AuthorizationsListInteractorV1
import com.saltedge.authenticator.features.authorizations.list.AuthorizationsListInteractorV2
import com.saltedge.authenticator.features.authorizations.list.AuthorizationsListViewModel
import com.saltedge.authenticator.features.connections.create.ConnectProviderInteractorV1
import com.saltedge.authenticator.features.connections.create.ConnectProviderInteractorV2
import com.saltedge.authenticator.features.connections.create.ConnectProviderViewModel
import com.saltedge.authenticator.features.connections.list.ConnectionsListInteractor
import com.saltedge.authenticator.features.connections.list.ConnectionsListViewModel
import com.saltedge.authenticator.features.connections.select.SelectConnectionsViewModel
import com.saltedge.authenticator.features.consents.details.ConsentDetailsInteractor
import com.saltedge.authenticator.features.consents.details.ConsentDetailsViewModel
import com.saltedge.authenticator.features.consents.list.ConsentsListInteractor
import com.saltedge.authenticator.features.consents.list.ConsentsListViewModel
import com.saltedge.authenticator.features.launcher.LauncherViewModel
import com.saltedge.authenticator.features.main.MainActivityInteractor
import com.saltedge.authenticator.features.main.MainActivityViewModel
import com.saltedge.authenticator.features.onboarding.OnboardingSetupViewModel
import com.saltedge.authenticator.features.qr.QrScannerViewModel
import com.saltedge.authenticator.features.settings.about.AboutViewModel
import com.saltedge.authenticator.features.settings.licenses.LicensesViewModel
import com.saltedge.authenticator.features.settings.list.SettingsListInteractorV1
import com.saltedge.authenticator.features.settings.list.SettingsListInteractorV2
import com.saltedge.authenticator.features.settings.list.SettingsListViewModel
import com.saltedge.authenticator.features.settings.passcode.PasscodeEditViewModel
import com.saltedge.authenticator.models.location.DeviceLocationManager
import com.saltedge.authenticator.models.realm.RealmManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.tools.CryptoToolsV1Abs
import com.saltedge.authenticator.sdk.v2.ScaServiceClient
import com.saltedge.authenticator.sdk.v2.tools.CryptoToolsV2Abs
import com.saltedge.authenticator.tools.PasscodeToolsAbs
import kotlinx.coroutines.Dispatchers
import java.lang.ref.WeakReference
import javax.inject.Inject

class ViewModelsFactory @Inject constructor(
    val appContext: Context,
    val passcodeTools: PasscodeToolsAbs,
    val biometricTools: BiometricToolsAbs,
    val cryptoToolsV1: CryptoToolsV1Abs,
    val cryptoToolsV2: CryptoToolsV2Abs,
    val preferenceRepository: PreferenceRepositoryAbs,
    val connectionsRepository: ConnectionsRepositoryAbs,
    val keyStoreManager: KeyManagerAbs,
    val realmManager: RealmManagerAbs,
    val apiManagerV1: AuthenticatorApiManagerAbs,
    val apiManagerV2: ScaServiceClient,
    val connectivityReceiver: ConnectivityReceiverAbs,
    val pushTokenUpdater: PushTokenUpdater
) : ViewModelProvider.Factory {

    private var _scaApiVersion: String = "1"
    private val scaApiV2IsRequired: Boolean
        get() = _scaApiVersion == "2"

    fun setScaApiVersion(version: String?) {
        _scaApiVersion = version ?: "1"
    }

    @Suppress("UNCHECKED_CAST")
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
                    interactor = MainActivityInteractor(
                        keyStoreManager = keyStoreManager,
                        connectionsRepository = connectionsRepository,
                        apiManagerV1 = apiManagerV1,
                        apiManagerV2 = apiManagerV2,
                        preferenceRepository = preferenceRepository,
                        pushTokenUpdater = pushTokenUpdater
                    )
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
                return QrScannerViewModel(connectionsRepository = connectionsRepository) as T
            }
            modelClass.isAssignableFrom(AuthorizationsListViewModel::class.java) -> {
                return AuthorizationsListViewModel(
                    interactorV1 = AuthorizationsListInteractorV1(
                        connectionsRepository = connectionsRepository,
                        keyStoreManager = keyStoreManager,
                        cryptoTools = cryptoToolsV1,
                        apiManager = apiManagerV1,
                        defaultDispatcher = Dispatchers.Default
                    ),
                    interactorV2 = AuthorizationsListInteractorV2(
                        connectionsRepository = connectionsRepository,
                        keyStoreManager = keyStoreManager,
                        cryptoTools = cryptoToolsV2,
                        apiManager = apiManagerV2,
                        defaultDispatcher = Dispatchers.Default
                    ),
                    locationManager = DeviceLocationManager,
                    connectivityReceiver = connectivityReceiver
                ) as T
            }
            modelClass.isAssignableFrom(AuthorizationDetailsViewModel::class.java) -> {
                return AuthorizationDetailsViewModel(
                    interactorV1 = AuthorizationDetailsInteractorV1(
                        connectionsRepository = connectionsRepository,
                        keyStoreManager = keyStoreManager,
                        cryptoTools = cryptoToolsV1,
                        apiManager = apiManagerV1
                    ),
                    interactorV2 = AuthorizationDetailsInteractorV2(
                        connectionsRepository = connectionsRepository,
                        keyStoreManager = keyStoreManager,
                        cryptoTools = cryptoToolsV2,
                        apiManager = apiManagerV2
                    ),
                    locationManager = DeviceLocationManager
                ) as T
            }
            modelClass.isAssignableFrom(ConnectProviderViewModel::class.java) -> {
                return createConnectProviderViewModel() as T
            }
            modelClass.isAssignableFrom(ConnectionsListViewModel::class.java) -> {
                return ConnectionsListViewModel(
                    weakContext = WeakReference(appContext),
                    interactor = ConnectionsListInteractor(
                        connectionsRepository = connectionsRepository,
                        keyStoreManager = keyStoreManager,
                        v1ApiManager = apiManagerV1,
                        v2ApiManager = apiManagerV2,
                        cryptoTools = cryptoToolsV1,
                        defaultDispatcher = Dispatchers.Default
                    ),
                    locationManager = DeviceLocationManager,
                    connectivityReceiver = connectivityReceiver
                ) as T
            }
            modelClass.isAssignableFrom(ConsentsListViewModel::class.java) -> {
                return ConsentsListViewModel(
                    weakContext = WeakReference(appContext),
                    interactor = ConsentsListInteractor(
                        connectionsRepository = connectionsRepository,
                        keyStoreManager = keyStoreManager,
                        v1ApiManager = apiManagerV1,
                        v2ApiManager = apiManagerV2,
                        cryptoTools = cryptoToolsV1,
                        defaultDispatcher = Dispatchers.Default
                    )
                ) as T
            }
            modelClass.isAssignableFrom(ConsentDetailsViewModel::class.java) -> {
                return ConsentDetailsViewModel(
                    weakContext = WeakReference(appContext),
                    interactor = ConsentDetailsInteractor(
                        connectionsRepository = connectionsRepository,
                        keyStoreManager = keyStoreManager,
                        v1ApiManager = apiManagerV1,
                        v2ApiManager = apiManagerV2,
                    )
                ) as T
            }
            modelClass.isAssignableFrom(SubmitActionViewModel::class.java) -> {
                return SubmitActionViewModel(
                    appContext = appContext,
                    connectionsRepository = connectionsRepository,
                    keyStoreManager = keyStoreManager,
                    apiManagerV1 = apiManagerV1,
                    apiManagerV2 = apiManagerV2,
                    locationManager = DeviceLocationManager
                ) as T
            }
            modelClass.isAssignableFrom(SelectConnectionsViewModel::class.java) -> {
                return SelectConnectionsViewModel() as T
            }
            modelClass.isAssignableFrom(SettingsListViewModel::class.java) -> {
                return SettingsListViewModel(
                    appContext = appContext,
                    interactorV1 = SettingsListInteractorV1(
                        keyStoreManager = keyStoreManager,
                        connectionsRepository = connectionsRepository,
                        apiManager = apiManagerV1
                    ),
                    interactorV2 = SettingsListInteractorV2(
                        keyStoreManager = keyStoreManager,
                        connectionsRepository = connectionsRepository,
                        apiManager = apiManagerV2
                    ),
                    appTools = AppTools,
                    preferenceRepository = preferenceRepository
                ) as T
            }
            modelClass.isAssignableFrom(PasscodeEditViewModel::class.java) -> {
                return PasscodeEditViewModel(
                    passcodeTools = passcodeTools,
                    defaultDispatcher = Dispatchers.Default
                ) as T
            }
            modelClass.isAssignableFrom(AboutViewModel::class.java) -> {
                return AboutViewModel(appContext) as T
            }
            modelClass.isAssignableFrom(LicensesViewModel::class.java) -> {
                return LicensesViewModel(appContext) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private fun createConnectProviderViewModel(): ConnectProviderViewModel {
        val interactor = if (scaApiV2IsRequired) {
            ConnectProviderInteractorV2(
                appContext = appContext,
                keyStoreManager = keyStoreManager,
                preferenceRepository = preferenceRepository,
                connectionsRepository = connectionsRepository,
                apiManager = apiManagerV2,
                defaultDispatcher = Dispatchers.Default
            )
        } else {
            ConnectProviderInteractorV1(
                appContext = appContext,
                keyStoreManager = keyStoreManager,
                preferenceRepository = preferenceRepository,
                connectionsRepository = connectionsRepository,
                apiManager = apiManagerV1,
                defaultDispatcher = Dispatchers.Default
            )
        }
        return ConnectProviderViewModel(
            appContext = appContext,
            interactor = interactor,
            locationManager = DeviceLocationManager
        )
    }
}
