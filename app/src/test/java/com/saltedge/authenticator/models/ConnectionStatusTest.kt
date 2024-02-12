/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.models

import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.toConnectionStatus
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test

class ConnectionStatusTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        assertThat(
            ConnectionStatus.values(),
            equalTo(arrayOf(ConnectionStatus.INACTIVE, ConnectionStatus.ACTIVE))
        )
    }

    @Test
    @Throws(Exception::class)
    fun valueOfTest() {
        assertThat(ConnectionStatus.valueOf("INACTIVE"), equalTo(ConnectionStatus.INACTIVE))
        assertThat(ConnectionStatus.valueOf("ACTIVE"), equalTo(ConnectionStatus.ACTIVE))
    }

    @Test
    @Throws(Exception::class)
    fun stringToConnectionStatusTest() {
        assertThat("INACTIVE".toConnectionStatus(), equalTo(ConnectionStatus.INACTIVE))
        assertThat("ACTIVE".toConnectionStatus(), equalTo(ConnectionStatus.ACTIVE))
        Assert.assertNull("UNKNOWN".toConnectionStatus())
    }
}
