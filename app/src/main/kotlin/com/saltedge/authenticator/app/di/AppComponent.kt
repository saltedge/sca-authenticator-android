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
import com.saltedge.authenticator.features.authorizations.details.di.AuthorizationDetailsComponent
import com.saltedge.authenticator.features.authorizations.details.di.AuthorizationDetailsModule
import com.saltedge.authenticator.features.authorizations.list.di.AuthorizationsListComponent
import com.saltedge.authenticator.features.authorizations.list.di.AuthorizationsListModule
import com.saltedge.authenticator.features.actions.di.ActionComponent
import com.saltedge.authenticator.features.actions.di.ActionModule
import com.saltedge.authenticator.features.connections.connect.di.ConnectProviderComponent
import com.saltedge.authenticator.features.connections.connect.di.ConnectProviderModule
import com.saltedge.authenticator.features.connections.list.di.ConnectionsListComponent
import com.saltedge.authenticator.features.connections.list.di.ConnectionsListModule
import com.saltedge.authenticator.features.launcher.di.LauncherComponent
import com.saltedge.authenticator.features.launcher.di.LauncherModule
import com.saltedge.authenticator.features.onboarding.di.OnboardingSetupComponent
import com.saltedge.authenticator.features.onboarding.di.OnboardingSetupModule
import com.saltedge.authenticator.features.security.LockableActivity
import com.saltedge.authenticator.features.settings.language.di.LanguageSelectComponent
import com.saltedge.authenticator.features.settings.language.di.LanguageSelectModule
import com.saltedge.authenticator.features.settings.list.di.SettingsListComponent
import com.saltedge.authenticator.features.settings.list.di.SettingsListModule
import com.saltedge.authenticator.features.settings.passcode.di.PasscodeEditComponent
import com.saltedge.authenticator.features.settings.passcode.di.PasscodeEditModule
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.tools.biometric.BiometricToolsAbs
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.tool.secure.PasscodeToolsAbs
import com.saltedge.authenticator.widget.biometric.BiometricPromptAbs
import dagger.Component
import javax.inject.Singleton

@Component(modules = [AppModule::class])
@Singleton
interface AppComponent {
    fun appContext(): Context
    fun preferenceRepository(): PreferenceRepositoryAbs
    fun connectionsRepository(): ConnectionsRepositoryAbs
    fun keyStoreManager(): KeyStoreManagerAbs
    fun passcodeTools(): PasscodeToolsAbs
    fun biometricTools(): BiometricToolsAbs
    fun biometricPrompt(): BiometricPromptAbs?

    fun addLauncherModule(module: LauncherModule): LauncherComponent
    fun addOnboardingSetupModule(module: OnboardingSetupModule): OnboardingSetupComponent
    fun addConnectionsListModule(module: ConnectionsListModule): ConnectionsListComponent
    fun addConnectProviderModule(module: ConnectProviderModule): ConnectProviderComponent
    fun addActionModule(module: ActionModule): ActionComponent
    fun addAuthorizationsListModule(module: AuthorizationsListModule): AuthorizationsListComponent
    fun addAuthorizationDetailsModule(module: AuthorizationDetailsModule): AuthorizationDetailsComponent
    fun addSettingsListModule(module: SettingsListModule): SettingsListComponent
    fun addLanguageSelectModule(module: LanguageSelectModule): LanguageSelectComponent
    fun addPasscodeEditModule(module: PasscodeEditModule): PasscodeEditComponent

    fun inject(activity: LockableActivity)
}
