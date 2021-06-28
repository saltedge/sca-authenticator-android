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
package com.saltedge.authenticator.features.authorizations.common

import android.view.View
import com.saltedge.authenticator.TestAppTools
import com.saltedge.authenticator.core.api.model.DescriptionData
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationData
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationV2Data
import net.danlew.android.joda.JodaTimeAndroid
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthorizationItemViewModelTest {

    private lateinit var model: AuthorizationItemViewModel

    @Before
    fun setUp() {
        JodaTimeAndroid.init(TestAppTools.applicationContext)
        model = AuthorizationItemViewModel(
            authorizationID = "444",
            authorizationCode = "111",
            title = "title",
            description = DescriptionData(text = "description"),
            endTime = DateTime(),
            connectionID = "333",
            connectionName = "Demobank",
            connectionLogoUrl = "url",
            validSeconds = 300,
            startTime = DateTime(),
            apiVersion = "1",
            geolocationRequired = false
        )
    }

    @Test
    @Throws(Exception::class)
    fun joinFinalModelsTestCase1() {
        //given
        val oldList = emptyList<AuthorizationItemViewModel>()
        val newList = listOf(
            createModelByIndex(1),
            createModelByIndex(3),
            createModelByIndex(1).copy(authorizationID = "4"),
            createModelByIndex(2),
        )

        //when
        val result = oldList.merge(newViewModels = newList, newModelsApiVersion = API_V1_VERSION)

        //then
        val expectedList = listOf(
            createModelByIndex(1),
            createModelByIndex(1).copy(authorizationID = "4"),
            createModelByIndex(2),
            createModelByIndex(3)
        )
        assertThat(result, equalTo(expectedList))
    }

    @Test
    @Throws(Exception::class)
    fun joinFinalModelsTestCase2() {
        //given
        val oldList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3)
        )
        val newList = emptyList<AuthorizationItemViewModel>()

        //when
        val result = oldList.merge(newViewModels = newList, newModelsApiVersion = API_V1_VERSION)

        //then
        assertThat(result.size, equalTo(0))
    }

    @Test
    @Throws(Exception::class)
    fun joinFinalModelsTestCase3() {
        //given
        val newList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3)
        )
        val oldList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3)
        )

        //when
        val result = oldList.merge(newViewModels = newList, newModelsApiVersion = API_V1_VERSION)

        //then
        assertThat(result, equalTo(newList))
    }

    @Test
    @Throws(Exception::class)
    fun joinFinalModelsTestCase4() {
        //given
        val oldList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3).copy(status = AuthorizationStatus.UNAVAILABLE)
        )
        val newList = emptyList<AuthorizationItemViewModel>()

        //when
        val result = oldList.merge(newViewModels = newList, newModelsApiVersion = API_V1_VERSION)

        //then
        assertThat(result.size, equalTo(1))
        assertThat(result.first().status, equalTo(AuthorizationStatus.UNAVAILABLE))
    }

    @Test
    @Throws(Exception::class)
    fun joinFinalModelsTestCase5() {
        //given
        val oldList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3).copy(status = AuthorizationStatus.DENIED)
        )
        val newList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3)
        )

        //when
        val result = oldList.merge(newViewModels = newList, newModelsApiVersion = API_V1_VERSION)

        //then
        assertThat(
            result,
            equalTo(listOf(
                createModelByIndex(1),
                createModelByIndex(2),
                createModelByIndex(3).copy(status = AuthorizationStatus.DENIED)
            ))
        )
    }

    @Test
    @Throws(Exception::class)
    fun joinFinalModelsTestCase6() {
        //given
        val oldList = listOf(
            createModelByIndex(1).copy(connectionID = "x"),
            createModelByIndex(2).copy(connectionID = "x"),
            createModelByIndex(3).copy(connectionID = "x", status = AuthorizationStatus.DENIED)
        )
        val newList = listOf(
            createModelByIndex(1).copy(connectionID = "x"),
            createModelByIndex(2).copy(connectionID = "x"),
            createModelByIndex(3).copy(connectionID = "x")
        )

        //when
        val result = oldList.merge(newViewModels = newList, newModelsApiVersion = API_V1_VERSION)

        //then
        val expectedResult = listOf(
            createModelByIndex(1).copy(connectionID = "x"),
            createModelByIndex(2).copy(connectionID = "x"),
            createModelByIndex(3).copy(connectionID = "x", status = AuthorizationStatus.DENIED)
        )
        assertThat(result, equalTo(expectedResult))
    }

    @Test
    @Throws(Exception::class)
    fun joinFinalModelsTestCase7() {
        //given
        val oldList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3).copy(status = AuthorizationStatus.UNAVAILABLE)
        )
        val newList = listOf(
            createModelByIndex(1),
            createModelByIndex(2)
        )

        //when
        val result = oldList.merge(newViewModels = newList, newModelsApiVersion = API_V1_VERSION)

        //then
        assertThat(
            result,
            equalTo(listOf(
                createModelByIndex(1),
                createModelByIndex(2),
                createModelByIndex(3).copy(status = AuthorizationStatus.UNAVAILABLE)
            ))
        )
    }

    @Test
    @Throws(Exception::class)
    fun joinFinalModelsTestCase8() {
        //given
        val oldList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3).copy(status = AuthorizationStatus.UNAVAILABLE)
        )
        val newList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(4)
        )


        //when
        val result = oldList.merge(newViewModels = newList, newModelsApiVersion = API_V1_VERSION)

        //then
        assertThat(
            result,
            equalTo(listOf(
                createModelByIndex(1),
                createModelByIndex(2),
                createModelByIndex(3).copy(status = AuthorizationStatus.UNAVAILABLE),
                createModelByIndex(4)
            ))
        )
    }

    @Test
    @Throws(Exception::class)
    fun joinFinalModelsTestCase9() {
        //given
        val oldList = listOf(
            createModelByIndex(11),
            createModelByIndex(12),
            createModelByIndex(13).copy(status = AuthorizationStatus.CONFIRM_PROCESSING),
            createModelByIndex(14).copy(status = AuthorizationStatus.DENIED),
            createModelByIndex(21).copy(apiVersion = API_V2_VERSION),
            createModelByIndex(22).copy(status = AuthorizationStatus.CONFIRM_PROCESSING, apiVersion = API_V2_VERSION),
            createModelByIndex(23).copy(status = AuthorizationStatus.DENIED, apiVersion = API_V2_VERSION),
        )
        val newList = listOf(
            createModelByIndex(21).copy(apiVersion = API_V2_VERSION),
            createModelByIndex(22).copy(status = AuthorizationStatus.CONFIRM_PROCESSING, apiVersion = API_V2_VERSION),
            createModelByIndex(24).copy(apiVersion = API_V2_VERSION),
        )


        //when
        val result = oldList.merge(newViewModels = newList, newModelsApiVersion = API_V2_VERSION)

        //then
        assertThat(
            result,
            equalTo(listOf(
                createModelByIndex(11),
                createModelByIndex(12),
                createModelByIndex(13).copy(status = AuthorizationStatus.CONFIRM_PROCESSING),
                createModelByIndex(14).copy(status = AuthorizationStatus.DENIED),
                createModelByIndex(21).copy(apiVersion = API_V2_VERSION),
                createModelByIndex(22).copy(status = AuthorizationStatus.CONFIRM_PROCESSING, apiVersion = API_V2_VERSION),
                createModelByIndex(23).copy(status = AuthorizationStatus.DENIED, apiVersion = API_V2_VERSION),
                createModelByIndex(24).copy(apiVersion = API_V2_VERSION),
            ))
        )
    }

    private fun createModelByIndex(index: Int): AuthorizationItemViewModel {
        return AuthorizationItemViewModel(
            authorizationID = "$index",
            authorizationCode = "$index",
            title = "$index",
            description = DescriptionData(text = "$index"),
            endTime = DateTime(index.toLong()),
            connectionID = "$index",
            connectionName = "$index",
            connectionLogoUrl = "$index",
            validSeconds = 100,
            startTime = DateTime(index.toLong()),
            apiVersion = "1",
            geolocationRequired = false
        )
    }

    @Test
    @Throws(Exception::class)
    fun canBeAuthorizedTest() {
        val results = AuthorizationStatus.values().map {
            model.status = it
            it to model.canBeAuthorized
        }.toMap()

        assertThat(results, equalTo(
            mapOf(
                AuthorizationStatus.LOADING to false,
                AuthorizationStatus.PENDING to true,
                AuthorizationStatus.CONFIRM_PROCESSING to false,
                AuthorizationStatus.DENY_PROCESSING to false,
                AuthorizationStatus.CONFIRMED to false,
                AuthorizationStatus.DENIED to false,
                AuthorizationStatus.ERROR to false,
                AuthorizationStatus.TIME_OUT to false,
                AuthorizationStatus.UNAVAILABLE to false
            )
        ))
    }

    @Test
    @Throws(Exception::class)
    fun hasFinalModeTest() {
        val results = AuthorizationStatus.values().map {
            model.status = it
            it to model.hasFinalStatus
        }.toMap()

        assertThat(results, equalTo(
            mapOf(
                AuthorizationStatus.LOADING to false,
                AuthorizationStatus.PENDING to false,
                AuthorizationStatus.CONFIRM_PROCESSING to false,
                AuthorizationStatus.DENY_PROCESSING to false,
                AuthorizationStatus.CONFIRMED to true,
                AuthorizationStatus.DENIED to true,
                AuthorizationStatus.ERROR to true,
                AuthorizationStatus.TIME_OUT to true,
                AuthorizationStatus.UNAVAILABLE to true
            )
        ))
    }

    @Test
    @Throws(Exception::class)
    fun shouldBeDestroyedTest() {
        Assert.assertFalse(model.shouldBeDestroyed)

        model.destroyAt = DateTime.now().minusSeconds(1)

        Assert.assertTrue(model.shouldBeDestroyed)

        model.destroyAt = DateTime.now().plusSeconds(1)

        Assert.assertFalse(model.shouldBeDestroyed)
    }

    @Test
    @Throws(Exception::class)
    fun isExpiredTest() {
        val now = DateTime.now()

        Assert.assertFalse(model.copy(endTime = now.plusMinutes(1)).isExpired)
        Assert.assertTrue(model.copy(endTime = now.minusMinutes(1)).isExpired)
    }

    @Test
    @Throws(Exception::class)
    fun timeViewVisibilityTest() {
        val results = AuthorizationStatus.values().map {
            model.status = it
            it to model.timeViewVisibility
        }.toMap()

        assertThat(results, equalTo(
            mapOf(
                AuthorizationStatus.LOADING to View.INVISIBLE,
                AuthorizationStatus.PENDING to View.VISIBLE,
                AuthorizationStatus.CONFIRM_PROCESSING to View.VISIBLE,
                AuthorizationStatus.DENY_PROCESSING to View.VISIBLE,
                AuthorizationStatus.CONFIRMED to View.VISIBLE,
                AuthorizationStatus.DENIED to View.VISIBLE,
                AuthorizationStatus.ERROR to View.VISIBLE,
                AuthorizationStatus.TIME_OUT to View.VISIBLE,
                AuthorizationStatus.UNAVAILABLE to View.INVISIBLE
            )
        ))
    }

    @Test
    @Throws(Exception::class)
    fun ignoreTimeUpdateTest() {
        val results = AuthorizationStatus.values().map {
            model.status = it
            it to model.ignoreTimeUpdate
        }.toMap()

        assertThat(results, equalTo(
            mapOf(
                AuthorizationStatus.LOADING to true,
                AuthorizationStatus.PENDING to false,
                AuthorizationStatus.CONFIRM_PROCESSING to true,
                AuthorizationStatus.DENY_PROCESSING to true,
                AuthorizationStatus.CONFIRMED to true,
                AuthorizationStatus.DENIED to true,
                AuthorizationStatus.ERROR to true,
                AuthorizationStatus.TIME_OUT to true,
                AuthorizationStatus.UNAVAILABLE to true
            )
        ))
    }

    @Test
    @Throws(Exception::class)
    fun isNotExpiredTest() {
        val now = DateTime.now()

        Assert.assertTrue(model.copy(endTime = now.plusMinutes(1)).isNotExpired)
        Assert.assertFalse(model.copy(endTime = now.minusMinutes(1)).isNotExpired)
    }

    @Test
    @Throws(Exception::class)
    fun remainedTimeTillExpireTest() {
        val now = DateTime.now()

        assertThat(model.copy(endTime = now.plusMinutes(1)).remainedTimeStringTillExpire,
                anyOf(equalTo("0:59"), equalTo("1:00")))
    }

    @Test
    @Throws(Exception::class)
    fun remainedSecondsTillExpireTest() {
        val now = DateTime.now()

        assertThat(
            model.copy(endTime = now.plusMinutes(1)).remainedSecondsTillExpire,
            anyOf(equalTo(59), equalTo(60))
        )
    }

    @Test
    @Throws(Exception::class)
    fun authorizationDataToAuthorizationViewModelTest() {
        val data = AuthorizationData(
            id = "444",
            authorizationCode = "111",
            title = "title",
            description = "description",
            createdAt = DateTime(0L),
            expiresAt = DateTime(300000L),
            connectionId = "333"
        )
        val connection = Connection().apply {
            id = "333"
            name = "Demobank"
            logoUrl = "url"
        }

        assertThat(
            data.toAuthorizationItemViewModel(connection = connection),
            equalTo(
                AuthorizationItemViewModel(
                    authorizationID = "444",
                    authorizationCode = "111",
                    title = "title",
                    description = DescriptionData(text = "description"),
                    endTime = DateTime(300000L),
                    connectionID = "333",
                    connectionName = "Demobank",
                    connectionLogoUrl = "url",
                    validSeconds = 300,
                    startTime = DateTime(0L),
                    apiVersion = "1",
                    status = AuthorizationStatus.PENDING,
                    geolocationRequired = connection.geolocationRequired ?: false
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun authorizationDataV2ToAuthorizationViewModelTest() {
        val data = AuthorizationV2Data(
            authorizationID = "444",
            authorizationCode = "111",
            title = "title",
            description = DescriptionData(text = "description"),
            createdAt = DateTime(0L),
            expiresAt = DateTime(300000L),
            connectionID = "333",
            status = "confirm_processing"
        )
        val connection = Connection().apply {
            id = "333"
            name = "Demobank"
            logoUrl = "url"
        }

        assertThat(
            data.toAuthorizationItemViewModel(connection = connection),
            equalTo(
                AuthorizationItemViewModel(
                    authorizationID = "444",
                    authorizationCode = "111",
                    title = "title",
                    description = DescriptionData(text = "description"),
                    endTime = DateTime(300000L),
                    connectionID = "333",
                    connectionName = "Demobank",
                    connectionLogoUrl = "url",
                    validSeconds = 300,
                    startTime = DateTime(0L),
                    apiVersion = "2",
                    status = AuthorizationStatus.CONFIRM_PROCESSING,
                    geolocationRequired = connection.geolocationRequired ?: false
                )
            )
        )
    }
}
