/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.authorizations.common

import com.saltedge.authenticator.TestAppTools
import com.saltedge.authenticator.core.api.model.DescriptionData
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import net.danlew.android.joda.JodaTimeAndroid
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthorizationItemJoinV1Test {

    @Before
    fun setUp() {
        JodaTimeAndroid.init(TestAppTools.applicationContext)
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
        val result = mergeV1(oldViewModels = oldList, newViewModels = newList)

        //then
        assertThat(result, equalTo(newList))
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
        val result = mergeV1(oldViewModels = oldList, newViewModels = newList)

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
        val result = mergeV1(oldViewModels = oldList, newViewModels = newList)

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
        val result = mergeV1(oldViewModels = oldList, newViewModels = newList)

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
        val result = mergeV1(oldViewModels = oldList, newViewModels = newList)

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
        val result = mergeV1(oldViewModels = oldList, newViewModels = newList)

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
        val result = mergeV1(oldViewModels = oldList, newViewModels = newList)

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
        val result = mergeV1(oldViewModels = oldList, newViewModels = newList)

        //then
        assertThat(
            result,
            equalTo(listOf(
                createModelByIndex(1),
                createModelByIndex(2),
                createModelByIndex(4),
                createModelByIndex(3).copy(status = AuthorizationStatus.UNAVAILABLE),
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
            apiVersion = API_V1_VERSION,
            geolocationRequired = false
        )
    }
}
