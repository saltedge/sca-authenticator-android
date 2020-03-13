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
package com.saltedge.authenticator.sdk.constants

// Static links
const val DEFAULT_HOST = "https://www.saltedge.com"
const val TERMS_LINK = "https://www.saltedge.com/pages/authenticator_terms"
const val DEFAULT_SUPPORT_EMAIL_LINK = "authenticator@saltedge.com"

// API endpoints
const val API_VERSION = "1"
private const val API_VERSION_NAMESPACE = "api/authenticator/v$API_VERSION"
const val API_CONNECTIONS = "$API_VERSION_NAMESPACE/connections"
const val API_AUTHORIZATIONS = "$API_VERSION_NAMESPACE/authorizations"
const val API_ACTION = "$API_VERSION_NAMESPACE/action"

const val DEFAULT_RETURN_URL = "authenticator://oauth/redirect"
const val DEFAULT_PLATFORM_NAME = "android"
const val DEFAULT_EXPIRATION_MINUTES = 5

// Model fields
const val KEY_DATA = "data"
const val KEY_PUBLIC_KEY = "public_key"
const val KEY_RETURN_URL = "return_url"
const val KEY_PUSH_TOKEN = "push_token"
const val KEY_CONNECT_QUERY = "connect_query"
const val KEY_PLATFORM = "platform"
const val KEY_ERROR_CLASS = "error_class"
const val KEY_ERROR_MESSAGE = "error_message"
const val KEY_IV = "iv"
const val KEY_KEY = "key"
const val KEY_ALGORITHM = "algorithm"

const val KEY_AUTHORIZATION_ID = "authorization_id"
const val KEY_CONNECTION_ID = "connection_id"
const val KEY_CONNECT_URL = "connect_url"
const val KEY_LOGO_URL = "logo_url"
const val KEY_ACCESS_TOKEN = "access_token"
const val KEY_CONFIRM = "confirm"
const val KEY_AUTHORIZATION_CODE = "authorization_code"
const val KEY_CODE = "code"
const val KEY_CREATED_AT = "created_at"
const val KEY_UPDATED_AT = "updated_at"

const val KEY_TITLE = "title"
const val KEY_DESCRIPTION = "description"
const val KEY_EXPIRES_AT = "expires_at"
const val KEY_ID = "id"
const val KEY_PROVIDER_CODE = "provider_code"
const val KEY_PROVIDER = "provider"
const val KEY_NAME = "name"
const val KEY_SUCCESS = "success"
const val KEY_VERSION = "version"
const val KEY_SUPPORT_EMAIL = "support_email"

// Error classes
const val ERROR_CLASS_AUTHENTICATION_RESPONSE = "AuthenticationError"
const val ERROR_CLASS_API_RESPONSE = "ApiResponseError"
const val ERROR_CLASS_API_REQUEST = "ApiRequestError"
const val ERROR_CLASS_HOST_UNREACHABLE = "HostUnreachable"
const val ERROR_CLASS_SSL_HANDSHAKE = "SSLHandshakeException"
const val ERROR_CLASS_CONNECTION_NOT_FOUND = "ConnectionNotFound"
const val ERROR_CLASS_AUTHORIZATION_NOT_FOUND = "AuthorizationNotFound"

// REQUEST METHODS
const val REQUEST_METHOD_GET = "GET"
const val REQUEST_METHOD_POST = "POST"
const val REQUEST_METHOD_PUT = "PUT"
const val REQUEST_METHOD_DELETE = "DELETE"
