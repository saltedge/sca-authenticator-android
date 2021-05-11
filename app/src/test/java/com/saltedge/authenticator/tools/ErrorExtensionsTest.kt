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
package com.saltedge.authenticator.tools

import com.saltedge.authenticator.TestAppTools
import com.saltedge.authenticator.core.api.ERROR_CLASS_API_RESPONSE
import com.saltedge.authenticator.core.api.ERROR_CLASS_CONNECTION_NOT_FOUND
import com.saltedge.authenticator.core.api.ERROR_CLASS_HOST_UNREACHABLE
import com.saltedge.authenticator.core.api.ERROR_CLASS_SSL_HANDSHAKE
import com.saltedge.authenticator.core.api.model.error.*
import com.saltedge.authenticator.sdk.R
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.ConnectException
import java.security.InvalidParameterException
import javax.net.ssl.SSLException

@RunWith(RobolectricTestRunner::class)
class ErrorExtensionsTest {

    @Test
    @Throws(Exception::class)
    fun getErrorMessageTest() {

        assertThat(
            ApiErrorData(
                errorClassName = "ErrorClass",
                errorMessage = "ErrorMessage"
            ).getErrorMessage(TestAppTools.applicationContext),
            equalTo("ErrorMessage")
        )
        assertThat(
            ApiErrorData(
                errorClassName = "ErrorClass",
                errorMessage = ""
            ).getErrorMessage(TestAppTools.applicationContext),
            equalTo("")
        )
        assertThat(
            ApiErrorData(
                errorClassName = ERROR_CLASS_HOST_UNREACHABLE,
                errorMessage = ""
            ).getErrorMessage(TestAppTools.applicationContext),
            equalTo(TestAppTools.getString(R.string.errors_no_connection))
        )
        assertThat(
            ApiErrorData(
                errorClassName = ERROR_CLASS_SSL_HANDSHAKE,
                errorMessage = ""
            ).getErrorMessage(TestAppTools.applicationContext),
            equalTo(TestAppTools.getString(R.string.errors_secure_connection))
        )
        assertThat(
            ApiErrorData(
                errorClassName = ERROR_CLASS_API_RESPONSE,
                errorMessage = ""
            ).getErrorMessage(TestAppTools.applicationContext),
            equalTo(TestAppTools.getString(R.string.errors_request_error))
        )
    }

    @Test
    @Throws(Exception::class)
    fun isConnectionNotFoundTest() {
        Assert.assertTrue(
            ApiErrorData(
                errorClassName = ERROR_CLASS_CONNECTION_NOT_FOUND,
                errorMessage = "ErrorMessage"
            ).isConnectionNotFound()
        )
        Assert.assertFalse(
            ApiErrorData(
                errorClassName = "ErrorClass",
                errorMessage = "ErrorMessage"
            ).isConnectionNotFound()
        )
    }

    @Test
    @Throws(Exception::class)
    fun isConnectivityErrorTest() {
        Assert.assertTrue(
            ApiErrorData(
                errorClassName = ERROR_CLASS_SSL_HANDSHAKE,
                errorMessage = "ErrorMessage"
            ).isConnectivityError()
        )
        Assert.assertTrue(
            ApiErrorData(
                errorClassName = ERROR_CLASS_HOST_UNREACHABLE,
                errorMessage = "ErrorMessage"
            ).isConnectivityError()
        )
        Assert.assertFalse(
            ApiErrorData(
                errorClassName = "ErrorClass",
                errorMessage = "ErrorMessage"
            ).isConnectivityError()
        )
    }

    @Test
    @Throws(Exception::class)
    fun createRequestErrorTest() {
        assertThat(
            createRequestError(404),
            equalTo(
                ApiErrorData(
                    errorClassName = ERROR_CLASS_API_RESPONSE,
                    errorMessage = "Request Error (404)"
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun createInvalidResponseErrorTest() {
        assertThat(
            createInvalidResponseError(),
            equalTo(
                ApiErrorData(
                    errorClassName = ERROR_CLASS_API_RESPONSE,
                    errorMessage = "Invalid response"
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun exceptionToApiErrorTest() {
        assertThat(
            ConnectException("").exceptionToApiError(),
            equalTo(ApiErrorData(errorClassName = ERROR_CLASS_HOST_UNREACHABLE))
        )
        assertThat(
            SSLException("").exceptionToApiError(),
            equalTo(ApiErrorData(errorClassName = ERROR_CLASS_SSL_HANDSHAKE))
        )
        assertThat(
            InvalidParameterException("").exceptionToApiError(),
            equalTo(ApiErrorData(errorClassName = ERROR_CLASS_API_RESPONSE))
        )
    }
}
