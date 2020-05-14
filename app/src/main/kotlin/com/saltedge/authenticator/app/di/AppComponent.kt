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
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.features.actions.di.SubmitActionComponent
import com.saltedge.authenticator.features.actions.di.SubmitActionModule
import com.saltedge.authenticator.features.authorizations.details.AuthorizationDetailsFragment
import com.saltedge.authenticator.features.authorizations.list.AuthorizationsListFragment
import com.saltedge.authenticator.features.connections.create.ConnectProviderFragment
import com.saltedge.authenticator.features.connections.list.di.ConnectionsListComponent
import com.saltedge.authenticator.features.connections.list.di.ConnectionsListModule
import com.saltedge.authenticator.features.launcher.LauncherActivity
import com.saltedge.authenticator.features.main.MainActivity
import com.saltedge.authenticator.features.onboarding.OnboardingSetupActivity
import com.saltedge.authenticator.features.qr.QrScannerActivity
import com.saltedge.authenticator.features.settings.about.AboutListFragment
import com.saltedge.authenticator.features.settings.language.di.LanguageSelectComponent
import com.saltedge.authenticator.features.settings.language.di.LanguageSelectModule
import com.saltedge.authenticator.features.settings.list.di.SettingsListComponent
import com.saltedge.authenticator.features.settings.list.di.SettingsListModule
import com.saltedge.authenticator.features.settings.passcode.di.PasscodeEditComponent
import com.saltedge.authenticator.features.settings.passcode.di.PasscodeEditModule
import com.saltedge.authenticator.models.realm.RealmManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.tools.biometric.BiometricToolsAbs
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.tools.PasscodeToolsAbs
import com.saltedge.authenticator.widget.biometric.BiometricPromptAbs
import com.saltedge.authenticator.widget.security.LockableActivity
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
    fun realmManager() : RealmManagerAbs
    fun viewModelsFactory() : ViewModelsFactory

    fun addConnectionsListModule(module: ConnectionsListModule): ConnectionsListComponent
    fun addActionModule(moduleSubmit: SubmitActionModule): SubmitActionComponent
    fun addSettingsListModule(module: SettingsListModule): SettingsListComponent
    fun addLanguageSelectModule(module: LanguageSelectModule): LanguageSelectComponent
    fun addPasscodeEditModule(module: PasscodeEditModule): PasscodeEditComponent

    fun inject(activity: LockableActivity)
    fun inject(activity: OnboardingSetupActivity)
    fun inject(activity: LauncherActivity)
    fun inject(activity: MainActivity)
    fun inject(activity: QrScannerActivity)
    fun inject(fragment: AuthorizationsListFragment)
    fun inject(fragment: AuthorizationDetailsFragment)
    fun inject(fragment: ConnectProviderFragment)
    fun inject(fragment: AboutListFragment)
}
