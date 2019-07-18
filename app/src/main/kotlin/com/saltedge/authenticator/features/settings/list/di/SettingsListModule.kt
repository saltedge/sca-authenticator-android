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
package com.saltedge.authenticator.features.settings.list.di

import android.content.Context
import com.saltedge.authenticator.app.di.FragmentScope
import com.saltedge.authenticator.features.settings.list.SettingsListContract
import com.saltedge.authenticator.features.settings.list.SettingsListPresenter
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.tool.secure.fingerprint.BiometricToolsAbs
import dagger.Module
import dagger.Provides

@Module
class SettingsListModule {

    @FragmentScope
    @Provides
    fun providePresenter(
            appContext: Context,
            preferences: PreferenceRepositoryAbs,
            biometricTools: BiometricToolsAbs
    ): SettingsListContract.Presenter {
        return SettingsListPresenter(
                appContext = appContext,
                preferences = preferences,
                biometricTools = biometricTools
        )
    }
}
