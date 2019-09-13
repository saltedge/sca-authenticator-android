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

import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.tools.KeyStoreManagerAbs
import com.saltedge.authenticator.testTools.TestAppTools
import com.saltedge.authenticator.tool.secure.fingerprint.BiometricToolsAbs
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsListModuleTest {

    @Test
    @Throws(Exception::class)
    fun providePresenterTest() {
        Assert.assertNotNull(
            SettingsListModule().providePresenter(
                TestAppTools.applicationContext,
                mockPreferences,
                mockBiometricTools,
                mockConnectionsRepository,
                mockKeyStoreManager
            )
        )
    }

    private val mockPreferences = Mockito.mock(PreferenceRepositoryAbs::class.java)
    private val mockBiometricTools = Mockito.mock(BiometricToolsAbs::class.java)
    private val mockConnectionsRepository = Mockito.mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = Mockito.mock(KeyStoreManagerAbs::class.java)
}
