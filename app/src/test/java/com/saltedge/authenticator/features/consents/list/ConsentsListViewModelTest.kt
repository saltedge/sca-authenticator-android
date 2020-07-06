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
package com.saltedge.authenticator.features.consents.list

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.connection.ConnectionStatus
import com.saltedge.authenticator.sdk.tools.crypt.CryptoToolsAbs
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.security.PrivateKey

//@RunWith(RobolectricTestRunner::class)
//class ConsentsListViewModelTest {
//
//    private lateinit var viewModel: ConsentsListViewModel
//    private val context: Context = ApplicationProvider.getApplicationContext()
//    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
//    private val mockKeyStoreManager = mock(KeyStoreManagerAbs::class.java)
//    private val mockApiManager = mock(AuthenticatorApiManagerAbs::class.java)
//    private val mockPrivateKey = Mockito.mock(PrivateKey::class.java)
//    private val mockCryptoTools = mock(CryptoToolsAbs::class.java)
//    private val connections = listOf(
//        Connection().apply {
//            guid = "guid2"
//            code = "demobank2"
//            name = "Demobank2"
//            status = "${ConnectionStatus.ACTIVE}"
//            accessToken = "token2"
//            createdAt = 300L
//            updatedAt = 300L
//        }
//    )
//    private val mockConnectionAndKey = ConnectionAndKey(connections[0], mockPrivateKey)
//
//    @Before
//    fun setUp() {
//        Mockito.doReturn(connections).`when`(mockConnectionsRepository).getAllConnections()
//        Mockito.doReturn(connections[0]).`when`(mockConnectionsRepository).getByGuid("guid2")
//        given(mockConnectionsRepository.getAllActiveConnections()).willReturn(listOf(connections[1]))
//        given(mockKeyStoreManager.createConnectionAndKeyModel(connections[1])).willReturn(
//            mockConnectionAndKey
//        )
//
//        viewModel = ConsentsListViewModel(
//            appContext = context,
//            connectionsRepository = mockConnectionsRepository,
//            keyStoreManager = mockKeyStoreManager,
//            apiManager = mockApiManager,
//            cryptoTools = mockCryptoTools
//        )
//    }
//}
