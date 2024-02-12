/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.app.di

import android.content.Context
import com.saltedge.authenticator.app.*
import com.saltedge.authenticator.cloud.PushTokenUpdater
import com.saltedge.authenticator.core.tools.biometric.BiometricTools
import com.saltedge.authenticator.core.tools.biometric.BiometricToolsAbs
import com.saltedge.authenticator.core.tools.secure.KeyManager
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.realm.RealmManager
import com.saltedge.authenticator.models.realm.RealmManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepository
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepository
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManager
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.tools.CryptoToolsV1
import com.saltedge.authenticator.sdk.tools.CryptoToolsV1Abs
import com.saltedge.authenticator.sdk.v2.ScaServiceClient
import com.saltedge.authenticator.sdk.v2.tools.CryptoToolsV2
import com.saltedge.authenticator.sdk.v2.tools.CryptoToolsV2Abs
import com.saltedge.authenticator.tools.PasscodeTools
import com.saltedge.authenticator.tools.PasscodeToolsAbs
import com.saltedge.authenticator.widget.biometric.BiometricPromptAbs
import com.saltedge.authenticator.widget.biometric.BiometricPromptManagerV28
import com.saltedge.authenticator.widget.biometric.BiometricsInputDialog
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(context: Context) {

    private var _context: Context = context
    private val preferenceRepository = PreferenceRepository.initObject(context)
    private val connectivityReceiver = ConnectivityReceiver(context)

    @Provides
    @Singleton
    fun provideAppContext(): Context = _context

    @Provides
    @Singleton
    fun provideBiometricTools(): BiometricToolsAbs = BiometricTools(_context, provideKeyStoreManager())

    @Provides
    fun provideBiometricPrompt(biometricTools: BiometricToolsAbs): BiometricPromptAbs? {
        return when {
            buildVersion28orGreater -> BiometricPromptManagerV28()
            buildVersion23orGreater -> BiometricsInputDialog(biometricTools)
            else -> null
        }
    }

    @Provides
    @Singleton
    fun provideViewModelsFactory(
        appContext: Context,
        passcodeTools: PasscodeToolsAbs,
        biometricTools: BiometricToolsAbs,
        cryptoToolsV1: CryptoToolsV1Abs,
        cryptoToolsV2: CryptoToolsV2Abs,
        preferences: PreferenceRepositoryAbs,
        connectionsRepository: ConnectionsRepositoryAbs,
        keyStoreManager: KeyManagerAbs,
        realmManager: RealmManagerAbs,
        apiManagerV1: AuthenticatorApiManagerAbs,
        apiManagerV2: ScaServiceClient,
        connectivityReceiver: ConnectivityReceiverAbs,
        pushTokenUpdater: PushTokenUpdater
    ): ViewModelsFactory {
        return ViewModelsFactory(
            appContext = appContext,
            passcodeTools = passcodeTools,
            biometricTools = biometricTools,
            cryptoToolsV1 = cryptoToolsV1,
            cryptoToolsV2 = cryptoToolsV2,
            preferenceRepository = preferences,
            connectionsRepository = connectionsRepository,
            keyStoreManager = keyStoreManager,
            realmManager = realmManager,
            apiManagerV1 = apiManagerV1,
            apiManagerV2 = apiManagerV2,
            connectivityReceiver = connectivityReceiver,
            pushTokenUpdater = pushTokenUpdater
        )
    }

    @Provides
    @Singleton
    fun providePasscodeTools(): PasscodeToolsAbs = PasscodeTools

    @Provides
    @Singleton
    fun provideRealmManager(): RealmManagerAbs = RealmManager

    @Provides
    @Singleton
    fun provideCryptoToolsV1(): CryptoToolsV1Abs = CryptoToolsV1

    @Provides
    @Singleton
    fun provideCryptoToolsV2(): CryptoToolsV2Abs = CryptoToolsV2

    @Provides
    @Singleton
    fun provideAuthenticatorApiManagerV1(): AuthenticatorApiManagerAbs = AuthenticatorApiManager

    @Provides
    @Singleton
    fun provideAuthenticatorApiManagerV2(): ScaServiceClient = ScaServiceClient()

    @Provides
    @Singleton
    fun provideConnectionsRepository(): ConnectionsRepositoryAbs = ConnectionsRepository

    @Provides
    @Singleton
    fun providePreferenceRepository(): PreferenceRepositoryAbs = preferenceRepository

    @Provides
    @Singleton
    fun provideKeyStoreManager(): KeyManagerAbs = KeyManager

    @Provides
    @Singleton
    fun provideConnectivityReceiver(): ConnectivityReceiverAbs = connectivityReceiver

    @Provides
    @Singleton
    fun providePushTokenUpdater(
        connectionsRepository: ConnectionsRepositoryAbs,
        keyStoreManager: KeyManagerAbs,
        apiManagerV2: ScaServiceClient,
        preferenceRepository: PreferenceRepositoryAbs
    ): PushTokenUpdater {
        return PushTokenUpdater(connectionsRepository, keyStoreManager, apiManagerV2, preferenceRepository)
    }
}
