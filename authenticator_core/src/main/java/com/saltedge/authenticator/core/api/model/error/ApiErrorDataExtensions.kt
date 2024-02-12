/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.api.model.error

import com.saltedge.authenticator.core.api.*
import com.saltedge.authenticator.core.tools.isNetworkException
import com.saltedge.authenticator.core.tools.isSSLException


/**
 * Creates Api Error from exception while receiving network response.
 * for Connection Exceptions set errorClassName as ERROR_CLASS_HOST_UNREACHABLE
 * for SSL Exceptions set errorClassName as ERROR_CLASS_SSL_HANDSHAKE
 *
 * @receiver throwable exception
 * @return api error
 */
fun Throwable.exceptionToApiError(): ApiErrorData {
    return when {
        this.isNetworkException() -> ApiErrorData(errorClassName = ERROR_CLASS_HOST_UNREACHABLE)
        this.isSSLException() -> ApiErrorData(errorClassName = ERROR_CLASS_SSL_HANDSHAKE)
        else -> ApiErrorData(errorClassName = ERROR_CLASS_API_RESPONSE)
    }
}

/**
 * Checks if errorClassName is equal to ERROR_CLASS_CONNECTION_NOT_FOUND
 *
 * @receiver api error
 * @return boolean. true if errorClassName == ERROR_CLASS_CONNECTION_NOT_FOUND
 */
fun ApiErrorData.isConnectionNotFound(): Boolean =
    errorClassName == ERROR_CLASS_CONNECTION_NOT_FOUND

/**
 * Checks if errorClassName is equal to ERROR_CLASS_CONNECTION_REVOKED
 *
 * @receiver api error
 * @return boolean. true if errorClassName == ERROR_CLASS_CONNECTION_REVOKED
 */
fun ApiErrorData.isConnectionRevoked(): Boolean =
    errorClassName == ERROR_CLASS_CONNECTION_REVOKED

/**
 * Checks if errorClassName is equal to ERROR_CLASS_AUTHORIZATION_NOT_FOUND
 *
 * @receiver api error
 * @return boolean. true if errorClassName == ERROR_CLASS_AUTHORIZATION_NOT_FOUND
 */
fun ApiErrorData.isAuthorizationNotFound(): Boolean =
    errorClassName == ERROR_CLASS_AUTHORIZATION_NOT_FOUND

/**
 * Checks if errorClassName is equal to ERROR_CLASS_SSL_HANDSHAKE or ERROR_CLASS_HOST_UNREACHABLE
 *
 * @receiver api error
 * @return boolean. true if errorClassName == ERROR_CLASS_SSL_HANDSHAKE || errorClassName == ERROR_CLASS_HOST_UNREACHABLE
 */
fun ApiErrorData.isConnectivityError(): Boolean =
    errorClassName == ERROR_CLASS_SSL_HANDSHAKE || errorClassName == ERROR_CLASS_HOST_UNREACHABLE

/**
 * Creates Api Error for unknown network error
 * with class name `ERROR_CLASS_API_RESPONSE` and message `Request Error ($responseCode)`
 *
 * @param responseCode - network response code (e.g. 200, 400, etc.)
 * @return api error
 */
fun createRequestError(responseCode: Int): ApiErrorData {
    return ApiErrorData(
        errorMessage = "Request Error ($responseCode)",
        errorClassName = ERROR_CLASS_API_RESPONSE
    )
}

/**
 * Creates Api Error for invalid response format
 * with class name `ERROR_CLASS_API_RESPONSE` and message `Invalid response`
 *
 * @return api error
 */
fun createInvalidResponseError(): ApiErrorData {
    return ApiErrorData(
        errorMessage = "Invalid response",
        errorClassName = ERROR_CLASS_API_RESPONSE
    )
}
