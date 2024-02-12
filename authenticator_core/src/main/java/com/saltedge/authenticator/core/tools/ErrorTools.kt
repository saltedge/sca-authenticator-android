/*
 * Copyright (c) 2021 Salt Edge Inc.
 */

package com.saltedge.authenticator.core.tools

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
