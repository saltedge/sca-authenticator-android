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
package com.saltedge.authenticator.app.di

import android.content.Context
import com.saltedge.authenticator.model.db.ConnectionsRepository
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.model.repository.PreferenceRepository
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManager
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.tools.CryptoTools
import com.saltedge.authenticator.sdk.tools.CryptoToolsAbs
import com.saltedge.authenticator.sdk.tools.KeyStoreManager
import com.saltedge.authenticator.sdk.tools.KeyStoreManagerAbs
import com.saltedge.authenticator.tool.AppTools
import com.saltedge.authenticator.tool.secure.PasscodeTools
import com.saltedge.authenticator.tool.secure.PasscodeToolsAbs
import com.saltedge.authenticator.tool.secure.fingerprint.BiometricTools
import com.saltedge.authenticator.tool.secure.fingerprint.BiometricToolsAbs
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

    @Provides
    @Singleton
    fun provideAppContext(): Context = _context

    @Provides
    @Singleton
    fun provideBiometricTools(): BiometricToolsAbs = BiometricTools

    @Provides
    fun provideBiometricPrompt(): BiometricPromptAbs {
        return if (AppTools.isBiometricPromptV28Enabled()) BiometricPromptManagerV28()
        else BiometricsInputDialog()
    }

    @Provides
    @Singleton
    fun providePasscodeTools(): PasscodeToolsAbs = PasscodeTools

    @Provides
    @Singleton
    fun provideCryptoTools(): CryptoToolsAbs = CryptoTools

    @Provides
    @Singleton
    fun provideAuthenticatorApiManager(): AuthenticatorApiManagerAbs = AuthenticatorApiManager

    @Provides
    @Singleton
    fun provideConnectionsRepository(): ConnectionsRepositoryAbs = ConnectionsRepository

    @Provides
    @Singleton
    fun providePreferenceRepository(): PreferenceRepositoryAbs = preferenceRepository

    @Provides
    @Singleton
    fun provideKeyStoreManager(): KeyStoreManagerAbs = KeyStoreManager
}
