/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
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
package com.saltedge.authenticator

import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.android.test_tools.encryptWithTestKey
import com.saltedge.authenticator.core.api.model.ConsentData
import com.saltedge.authenticator.core.api.model.ConsentSharedData
import com.saltedge.authenticator.core.api.model.EncryptedData
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.BaseCryptoToolsAbs
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.mockito.BDDMockito

object TestFactory {

    val mockPrivateKey = CommonTestTools.testPrivateKey

    val connection1 = Connection().apply {
        id = "1"
        guid = "guid1"
        code = "demobank1"
        name = "Demobank1"
        status = "${ConnectionStatus.ACTIVE}"
        accessToken = "token1"
        supportEmail = "example@example.com"
        logoUrl = "https://www.fentury.com/"
        createdAt = 100L
        updatedAt = 100L
        apiVersion = API_V1_VERSION
        geolocationRequired = true
    }
    val connection2 = Connection().apply {
        id = "2"
        guid = "guid2"
        code = "demobank2"
        name = "Demobank2"
        status = "${ConnectionStatus.ACTIVE}"
        supportEmail = "example@example.com"
        logoUrl = "https://www.fentury.com/"
        accessToken = "token2"
        createdAt = 200L
        updatedAt = 200L
        apiVersion = API_V2_VERSION
    }
    val connection3Inactive = Connection().apply {
        id = "3"
        guid = "guid3"
        code = "demobank3"
        name = "Demobank3"
        status = "${ConnectionStatus.INACTIVE}"
        supportEmail = "example@example.com"
        logoUrl = "https://www.fentury.com/"
        accessToken = "token3"
        createdAt = 300L
        updatedAt = 300L
        apiVersion = API_V1_VERSION
    }
    val connection4 = Connection().apply {
        id = "4"
        guid = "guid4"
        code = "demobank4"
        name = "Demobank4"
        status = "${ConnectionStatus.ACTIVE}"
        supportEmail = "example@example.com"
        logoUrl = "https://www.saltedge.com/"
        accessToken = "token4"
        createdAt = 400L
        updatedAt = 400L
        apiVersion = API_V2_VERSION
    }

    val allConnections = listOf(connection1, connection2, connection3Inactive, connection4)
    val allActiveConnections = listOf(connection1, connection2, connection4)

    val richConnection1 = RichConnection(connection1, mockPrivateKey)
    val richConnection2 = RichConnection(connection2, mockPrivateKey)
    val richConnection3 = RichConnection(connection3Inactive, mockPrivateKey)
    val richConnection4 = RichConnection(connection4, mockPrivateKey)

    val v1AispConsentData = ConsentData(
        id = "111",
        connectionId = connection1.id,
        connectionGuid = connection1.guid,
        userId = "1",
        tppName = "tppName111",
        consentTypeString = "aisp",
        accounts = emptyList(),
        expiresAt = DateTime(0).withZone(DateTimeZone.UTC),
        createdAt = DateTime(0).withZone(DateTimeZone.UTC),
        sharedData = ConsentSharedData(balance = true, transactions = true)
    )
    val v1PispFutureConsentData = ConsentData(
        id = "112",
        connectionId = connection1.id,
        connectionGuid = connection1.guid,
        userId = "1",
        tppName = "tppName112",
        consentTypeString = "pisp_future",
        accounts = emptyList(),
        expiresAt = DateTime(0).withZone(DateTimeZone.UTC),
        createdAt = DateTime(0).withZone(DateTimeZone.UTC),
        sharedData = ConsentSharedData(balance = true, transactions = true)
    )
    val v1PispRecurringConsentData = ConsentData(
        id = "113",
        connectionId = connection1.id,
        connectionGuid = connection1.guid,
        userId = "1",
        tppName = "tppName113",
        consentTypeString = "pisp_recurring",
        accounts = emptyList(),
        expiresAt = DateTime(0).withZone(DateTimeZone.UTC),
        createdAt = DateTime(0).withZone(DateTimeZone.UTC),
        sharedData = ConsentSharedData(balance = true, transactions = true)
    )
    val v2ConsentData: ConsentData = ConsentData(
        id = "222",
        connectionId = connection2.id,
        connectionGuid = connection2.guid,
        tppName = "tppName222",
        consentTypeString = "aisp",
        accounts = emptyList(),
        expiresAt = DateTime(0).withZone(DateTimeZone.UTC),
        createdAt = DateTime(0).withZone(DateTimeZone.UTC),
        sharedData = ConsentSharedData(balance = true, transactions = true)
    )
    val v1Consents = listOf(v1AispConsentData, v1PispFutureConsentData, v1PispRecurringConsentData)
    val v2Consents = listOf(v2ConsentData)
    val allConsents = v1Consents + v2Consents
    val encV1Consents: List<EncryptedData> = v1Consents.map { it.encryptWithTestKey() }
    val encV2Consents: List<EncryptedData> = v2Consents.map { it.encryptWithTestKey() }

    fun mockConnections(mock: ConnectionsRepositoryAbs) {
        BDDMockito.given(mock.getByGuid(connection1.guid)).willReturn(connection1)
        BDDMockito.given(mock.getByGuid(connection2.guid)).willReturn(connection2)
        BDDMockito.given(mock.getByGuid(connection3Inactive.guid)).willReturn(connection3Inactive)
        BDDMockito.given(mock.getByGuid(connection4.guid)).willReturn(connection4)
        BDDMockito.given(mock.getAllConnections()).willReturn(allConnections)
    }

    fun mockRichConnections(mock: KeyManagerAbs) {
        BDDMockito.given(mock.enrichConnection(connection1, addProviderKey = false))
            .willReturn(richConnection1)
        BDDMockito.given(mock.enrichConnection(connection2, addProviderKey = true))
            .willReturn(richConnection2)
        BDDMockito.given(mock.enrichConnection(connection3Inactive, addProviderKey = false))
            .willReturn(richConnection3)
        BDDMockito.given(mock.enrichConnection(connection4, addProviderKey = true))
            .willReturn(richConnection4)
    }

    fun mockConsents(mock: BaseCryptoToolsAbs) {
        encV1Consents.forEachIndexed { index, encryptedData ->
            BDDMockito.given(
                mock.decryptConsentData(
                    encryptedData = encryptedData,
                    rsaPrivateKey = mockPrivateKey,
                    connectionGUID = connection1.guid,
                    consentID = null
                )
            ).willReturn(v1Consents[index])
        }
        encV2Consents.forEachIndexed { index, encryptedData ->
            BDDMockito.given(
                mock.decryptConsentData(
                    encryptedData = encryptedData,
                    rsaPrivateKey = mockPrivateKey,
                    connectionGUID = connection2.guid,
                    consentID = encryptedData.id
                )
            ).willReturn(v2Consents[index])
        }
    }
}
