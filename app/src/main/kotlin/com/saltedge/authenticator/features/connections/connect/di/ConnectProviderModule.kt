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
package com.saltedge.authenticator.features.connections.connect.di

import android.content.Context
import com.saltedge.authenticator.app.di.FragmentScope
import com.saltedge.authenticator.features.connections.connect.ConnectProviderContract
import com.saltedge.authenticator.features.connections.connect.ConnectProviderPresenter
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManager
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import dagger.Module
import dagger.Provides

@Module
class ConnectProviderModule {

    @FragmentScope
    @Provides
    fun providePresenter(
        appContext: Context,
        preferences: PreferenceRepositoryAbs,
        connections: ConnectionsRepositoryAbs,
        keyStoreManager: KeyStoreManagerAbs
    ): ConnectProviderContract.Presenter {
        return ConnectProviderPresenter(
            appContext = appContext,
            preferenceRepository = preferences,
            connectionsRepository = connections,
            keyStoreManager = keyStoreManager,
            apiManager = AuthenticatorApiManager
        )
    }
}
