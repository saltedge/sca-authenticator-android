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

import com.saltedge.authenticator.model.db.Connection
import com.saltedge.authenticator.sdk.model.authorization.AuthorizationData
import com.saltedge.authenticator.testTools.TestAppTools
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
class AuthorizationViewModelTest {

    @Before
    fun setUp() {
        JodaTimeAndroid.init(TestAppTools.applicationContext)
    }

    private val model = AuthorizationViewModel(
        authorizationID = "444",
        authorizationCode = "111",
        title = "title",
        description = "description",
        expiresAt = DateTime(),
        connectionID = "333",
        connectionName = "Demobank",
        connectionLogoUrl = "url",
        validSeconds = 300,
        createdAt = DateTime()
    )

    @Test
    @Throws(Exception::class)
    fun joinFinalModelsTestCase1() {
        val newList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3)
        )
        val oldList = emptyList<AuthorizationViewModel>()

        assertThat(newList.joinFinalModels(oldList), equalTo(newList))
    }

    @Test
    @Throws(Exception::class)
    fun joinFinalModelsTestCase2() {
        val newList = emptyList<AuthorizationViewModel>()
        val oldList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3).copy(viewMode = ViewMode.UNAVAILABLE)
        )

        assertThat(newList.joinFinalModels(oldList)[0].viewMode,
            equalTo(ViewMode.UNAVAILABLE))
    }

    @Test
    @Throws(Exception::class)
    fun joinFinalModelsTestCase3() {
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

        assertThat(newList.joinFinalModels(oldList), equalTo(newList))
    }

    @Test
    @Throws(Exception::class)
    fun joinFinalModelsTestCase4() {
        val newList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3)
        )
        val oldList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3).copy(viewMode = ViewMode.DENY_SUCCESS)
        )
        val resultList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3).copy(viewMode = ViewMode.DENY_SUCCESS)
        )

        assertThat(newList.joinFinalModels(oldList), equalTo(resultList))
    }

    @Test
    @Throws(Exception::class)
    fun joinFinalModelsTestCase5() {
        val newList = listOf(
            createModelByIndex(1).copy(connectionID = "x"),
            createModelByIndex(2).copy(connectionID = "x"),
            createModelByIndex(3).copy(connectionID = "x")
        )
        val oldList = listOf(
            createModelByIndex(1).copy(connectionID = "x"),
            createModelByIndex(2).copy(connectionID = "x"),
            createModelByIndex(3).copy(connectionID = "x", viewMode = ViewMode.DENY_SUCCESS)
        )
        val resultList = listOf(
            createModelByIndex(1).copy(connectionID = "x"),
            createModelByIndex(2).copy(connectionID = "x"),
            createModelByIndex(3).copy(connectionID = "x", viewMode = ViewMode.DENY_SUCCESS)
        )

        assertThat(newList.joinFinalModels(oldList), equalTo(resultList))
    }

    @Test
    @Throws(Exception::class)
    fun joinFinalModelsTestCase6() {
        val newList = listOf(
            createModelByIndex(1),
            createModelByIndex(2)
        )
        val oldList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3).copy(viewMode = ViewMode.UNAVAILABLE)
        )
        val resultList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3).copy(viewMode = ViewMode.UNAVAILABLE)
        )

        assertThat(newList.joinFinalModels(oldList), equalTo(resultList))
        assertThat(newList.joinFinalModels(oldList)[2].viewMode,
            equalTo(ViewMode.UNAVAILABLE))
    }

    @Test
    @Throws(Exception::class)
    fun joinFinalModelsTestCase7() {
        val newList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(4)
        )
        val oldList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3).copy(viewMode = ViewMode.UNAVAILABLE)
        )
        val resultList = listOf(
            createModelByIndex(1),
            createModelByIndex(2),
            createModelByIndex(3).copy(viewMode = ViewMode.UNAVAILABLE),
            createModelByIndex(4)
        )

        assertThat(newList.joinFinalModels(oldList), equalTo(resultList))
        assertThat(newList.joinFinalModels(oldList)[2].viewMode,
            equalTo(ViewMode.UNAVAILABLE))
    }

    private fun createModelByIndex(index: Int): AuthorizationViewModel {
        return AuthorizationViewModel(
            authorizationID = "$index",
            authorizationCode = "$index",
            title = "$index",
            description = "$index",
            expiresAt = DateTime(index.toLong()),
            connectionID = "$index",
            connectionName = "$index",
            connectionLogoUrl = "$index",
            validSeconds = 100,
            createdAt = DateTime(index.toLong())
        )
    }

    @Test
    @Throws(Exception::class)
    fun canBeAuthorizedTest() {
        val results = ViewMode.values().map {
            model.viewMode = it
            it to model.canBeAuthorized
        }.toMap()

        assertThat(results, equalTo(
            mapOf(
                ViewMode.LOADING to false,
                ViewMode.DEFAULT to true,
                ViewMode.CONFIRM_PROCESSING to false,
                ViewMode.DENY_PROCESSING to false,
                ViewMode.CONFIRM_SUCCESS to false,
                ViewMode.DENY_SUCCESS to false,
                ViewMode.ERROR to false,
                ViewMode.TIME_OUT to false,
                ViewMode.UNAVAILABLE to false
            )
        ))
    }

    @Test
    @Throws(Exception::class)
    fun hasFinalModeTest() {
        val results = ViewMode.values().map {
            model.viewMode = it
            it to model.hasFinalMode
        }.toMap()

        assertThat(results, equalTo(
            mapOf(
                ViewMode.LOADING to false,
                ViewMode.DEFAULT to false,
                ViewMode.CONFIRM_PROCESSING to false,
                ViewMode.DENY_PROCESSING to false,
                ViewMode.CONFIRM_SUCCESS to true,
                ViewMode.DENY_SUCCESS to true,
                ViewMode.ERROR to true,
                ViewMode.TIME_OUT to true,
                ViewMode.UNAVAILABLE to true
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

        Assert.assertFalse(model.copy(expiresAt = now.plusMinutes(1)).isExpired)
        Assert.assertTrue(model.copy(expiresAt = now.minusMinutes(1)).isExpired)
    }

    @Test
    @Throws(Exception::class)
    fun ignoreTimeUpdateTest() {
        val results = ViewMode.values().map {
            model.viewMode = it
            it to model.ignoreTimeUpdate
        }.toMap()

        assertThat(results, equalTo(
            mapOf(
                ViewMode.LOADING to true,
                ViewMode.DEFAULT to false,
                ViewMode.CONFIRM_PROCESSING to true,
                ViewMode.DENY_PROCESSING to true,
                ViewMode.CONFIRM_SUCCESS to true,
                ViewMode.DENY_SUCCESS to true,
                ViewMode.ERROR to true,
                ViewMode.TIME_OUT to true,
                ViewMode.UNAVAILABLE to true
            )
        ))
    }

    @Test
    @Throws(Exception::class)
    fun isNotExpiredTest() {
        val now = DateTime.now()

        Assert.assertTrue(model.copy(expiresAt = now.plusMinutes(1)).isNotExpired)
        Assert.assertFalse(model.copy(expiresAt = now.minusMinutes(1)).isNotExpired)
    }

    @Test
    @Throws(Exception::class)
    fun remainedTimeTillExpireTest() {
        val now = DateTime.now()

        assertThat(model.copy(expiresAt = now.plusMinutes(1)).remainedTimeStringTillExpire,
                anyOf(equalTo("0:59"), equalTo("1:00")))
    }

    @Test
    @Throws(Exception::class)
    fun remainedSecondsTillExpireTest() {
        val now = DateTime.now()

        assertThat(
            model.copy(expiresAt = now.plusMinutes(1)).remainedSecondsTillExpire,
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
            data.toAuthorizationViewModel(connection = connection),
            equalTo(
                AuthorizationViewModel(
                    authorizationID = "444",
                    authorizationCode = "111",
                    title = "title",
                    description = "description",
                    expiresAt = DateTime(300000L),
                    connectionID = "333",
                    connectionName = "Demobank",
                    connectionLogoUrl = "url",
                    validSeconds = 300,
                    createdAt = DateTime(0L)
                )
            )
        )
    }
}
