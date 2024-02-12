/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.constants

// Static links
const val DEFAULT_HOST = "https://www.saltedge.com"

// API endpoints
const val API_V1_VERSION = "1"
private const val API_VERSION_NAMESPACE = "api/authenticator/v$API_V1_VERSION"
const val API_CONNECTIONS = "$API_VERSION_NAMESPACE/connections"
const val API_AUTHORIZATIONS = "$API_VERSION_NAMESPACE/authorizations"
const val API_ACTIONS = "$API_VERSION_NAMESPACE/actions"
const val API_CONSENTS = "$API_VERSION_NAMESPACE/consents"

// REQUEST METHODS
const val REQUEST_METHOD_GET = "GET"
const val REQUEST_METHOD_POST = "POST"
const val REQUEST_METHOD_PUT = "PUT"
const val REQUEST_METHOD_DELETE = "DELETE"
