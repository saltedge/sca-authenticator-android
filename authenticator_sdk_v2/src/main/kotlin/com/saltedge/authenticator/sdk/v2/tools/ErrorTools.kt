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

package com.saltedge.authenticator.sdk.v2.tools

private val NETWORK_EXCEPTIONS = listOf(
    "ConnectException",
    "UnknownHostException",
    "SocketTimeoutException",
    "IOException",
    "SocketException",
    "NoRouteToHostException",
    "InterruptedIOException"
)

private val SSL_EXCEPTIONS = listOf(
    "SSLHandshakeException",
    "SSLException",
    "SSLPeerUnverifiedException",
    "UnknownServiceException"
)

/**
 * Checks if raised network related exception
 *
 * @receiver throwable exception
 * @return boolean, true if in class we contains list of connection exception
 * @see NETWORK_EXCEPTIONS
 */
fun Throwable.isNetworkException(): Boolean =
    NETWORK_EXCEPTIONS.contains(this.javaClass.simpleName)

/**
 * Checks if raised SSL exception
 *
 * @receiver throwable exception
 * @return boolean, true if in class we contains list of ssl exception
 * @see SSL_EXCEPTIONS
 */
fun Throwable.isSSLException(): Boolean = SSL_EXCEPTIONS.contains(this.javaClass.simpleName)
