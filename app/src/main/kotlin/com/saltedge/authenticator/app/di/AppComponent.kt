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
import com.saltedge.authenticator.features.actions.SubmitActionFragment
import com.saltedge.authenticator.features.authorizations.details.AuthorizationDetailsFragment
import com.saltedge.authenticator.features.authorizations.list.AuthorizationsListFragment
import com.saltedge.authenticator.features.connections.create.ConnectProviderFragment
import com.saltedge.authenticator.features.connections.list.ConnectionsListFragment
import com.saltedge.authenticator.features.connections.select.SelectConnectionsFragment
import com.saltedge.authenticator.features.consents.details.ConsentDetailsFragment
import com.saltedge.authenticator.features.consents.list.ConsentsListFragment
import com.saltedge.authenticator.features.launcher.SplashFragment
import com.saltedge.authenticator.features.main.MainActivity
import com.saltedge.authenticator.features.onboarding.OnboardingFragment
import com.saltedge.authenticator.features.qr.QrScannerActivity
import com.saltedge.authenticator.features.settings.about.AboutListFragment
import com.saltedge.authenticator.features.settings.language.LanguageSelectDialog
import com.saltedge.authenticator.features.settings.licenses.LicensesFragment
import com.saltedge.authenticator.features.settings.list.SettingsListFragment
import com.saltedge.authenticator.models.realm.RealmManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.tools.PasscodeToolsAbs
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
    fun realmManager() : RealmManagerAbs
    fun viewModelsFactory() : ViewModelsFactory

    fun inject(activity: MainActivity)
    fun inject(activity: QrScannerActivity)
    fun inject(fragment: AuthorizationsListFragment)
    fun inject(fragment: AuthorizationDetailsFragment)
    fun inject(fragment: ConnectProviderFragment)
    fun inject(fragment: ConnectionsListFragment)
    fun inject(fragment: ConsentsListFragment)
    fun inject(fragment: SubmitActionFragment)
    fun inject(fragment: SelectConnectionsFragment)
    fun inject(fragment: SettingsListFragment)
    fun inject(fragment: AboutListFragment)
    fun inject(fragment: LicensesFragment)
    fun inject(fragment: LanguageSelectDialog)
    fun inject(fragment: ConsentDetailsFragment)
    fun inject(fragment: SplashFragment)
    fun inject(fragment: OnboardingFragment)
}
