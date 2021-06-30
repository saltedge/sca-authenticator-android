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
package com.saltedge.authenticator.features.authorizations.common

import com.saltedge.authenticator.TestAppTools
import com.saltedge.authenticator.core.api.model.DescriptionData
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import net.danlew.android.joda.JodaTimeAndroid
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthorizationItemJoinV2Test {

    @Before
    fun setUp() {
        JodaTimeAndroid.init(TestAppTools.applicationContext)
    }

    @Test
    @Throws(Exception::class)
    fun mergeV2TestCase1() {
        //given
        val oldList = emptyList<AuthorizationItemViewModel>()
        val newList = listOf(
            createModelByIndex(1),
            createModelByIndex(3),
            createModelByIndex(2),
        )

        //when
        val result = mergeV2(oldViewModels = oldList, newViewModels = newList)

        //then
        assertThat(result, equalTo(newList))
    }

    @Test
    @Throws(Exception::class)
    fun mergeV2TestCase2() {
        //given
        val oldList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3)
        )
        val newList = emptyList<AuthorizationItemViewModel>()

        //when
        val result = mergeV2(oldViewModels = oldList, newViewModels = newList)

        //then
        assertThat(result.size, equalTo(0))
    }

    @Test
    @Throws(Exception::class)
    fun mergeV2TestCase3() {
        //given
        val oldList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3),
        )
        val newList = listOf(
            createModelByIndex(1),
            createModelByIndex(3),
            createModelByIndex(2),
        )

        //when
        val result = mergeV2(oldViewModels = oldList, newViewModels = newList)

        //then
        assertThat(result, equalTo(newList))
    }

    @Test
    @Throws(Exception::class)
    fun mergeV2TestCase4() {
        //given
        val oldList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3).copy(status = AuthorizationStatus.DENY_PROCESSING)
        )
        val newList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createFinalModelByIndex(3, status = AuthorizationStatus.DENIED)
        )

        //when
        val result = mergeV2(oldViewModels = oldList, newViewModels = newList)

        //then
        val expectedResult = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3).copy(status = AuthorizationStatus.DENIED)
        )
        assertThat(result, equalTo(expectedResult))
    }

    @Test
    @Throws(Exception::class)
    fun mergeV2TestCase5() {
        //given
        val oldList = listOf(
            createModelByIndex(1).copy(connectionID = "x"),
            createModelByIndex(2).copy(connectionID = "x"),
            createModelByIndex(4).copy(status = AuthorizationStatus.CONFIRMED),
            createModelByIndex(5).copy(connectionID = "x", status = AuthorizationStatus.UNAVAILABLE),

        )
        val newList = listOf(
            createModelByIndex(1).copy(connectionID = "x"),
            createModelByIndex(2).copy(connectionID = "x"),
            createFinalModelByIndex(4, status = AuthorizationStatus.CONFIRMED)
        )

        //when
        val result = mergeV2(oldViewModels = oldList, newViewModels = newList)

        //then
        val expectedResult = listOf(
            createModelByIndex(1).copy(connectionID = "x"),
            createModelByIndex(2).copy(connectionID = "x"),
            createModelByIndex(4).copy(status = AuthorizationStatus.CONFIRMED)
        )
        assertThat(result, equalTo(expectedResult))
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
            apiVersion = API_V2_VERSION,
            geolocationRequired = true
        )
    }

    private fun createFinalModelByIndex(index: Int, status: AuthorizationStatus): AuthorizationItemViewModel {
        return AuthorizationItemViewModel(
            authorizationID = "$index",
            authorizationCode = "",
            title = "",
            description = DescriptionData(text = null),
            startTime = DateTime(0),
            endTime = DateTime(0),
            validSeconds = 0,
            connectionID = "$index",
            connectionName = "$index",
            connectionLogoUrl = "$index",
            apiVersion = API_V2_VERSION,
            geolocationRequired = true,
            status = status
        )
    }
}
