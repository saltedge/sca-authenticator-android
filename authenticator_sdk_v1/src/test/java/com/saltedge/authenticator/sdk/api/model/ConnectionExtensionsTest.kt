/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.model

import com.saltedge.android.test_tools.TestConnection
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.getStatus
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConnectionExtensionsTest {

    @Test
    @Throws(Exception::class)
    fun connectionGetStatusTest() {
        val connection = TestConnection().apply { status = "${ConnectionStatus.INACTIVE}" }

        assertThat(connection.status, equalTo("${ConnectionStatus.INACTIVE}"))
        assertThat(connection.getStatus(), equalTo(ConnectionStatus.INACTIVE))
    }
}
