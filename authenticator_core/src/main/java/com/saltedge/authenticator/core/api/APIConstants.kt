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
package com.saltedge.authenticator.core.api

const val TERMS_LINK = "https://www.saltedge.com/pages/authenticator_terms"
const val DEFAULT_SUPPORT_EMAIL_LINK = "authenticator@saltedge.com"
const val DEFAULT_RETURN_URL = "authenticator://oauth/redirect"
const val DEFAULT_PLATFORM_NAME = "android"
const val DEFAULT_EXPIRATION_MINUTES = 5

const val HEADER_CONTENT_TYPE = "Content-Type"
const val HEADER_KEY_ACCEPT_LANGUAGE = "Accept-Language"
const val HEADER_KEY_ACCESS_TOKEN = "Access-Token"
const val HEADER_KEY_USER_AGENT = "User-Agent"
const val HEADER_KEY_GEOLOCATION = "GEO-Location"
const val HEADER_KEY_AUTHORIZATION_TYPE = "Authorization-Type"
const val HEADER_VALUE_JSON = "application/json"
const val HEADER_VALUE_ACCEPT_LANGUAGE = "en"

const val KEY_DATA = "data"
const val KEY_EXP = "exp"
const val KEY_RETURN_URL = "return_url"
const val KEY_PUSH_TOKEN = "push_token"
const val KEY_CONNECT_QUERY = "connect_query"
const val KEY_PLATFORM = "platform"
const val KEY_ERROR_CLASS = "error_class"
const val KEY_ERROR_MESSAGE = "error_message"
const val KEY_IV = "iv"
const val KEY_KEY = "key"
const val KEY_ALGORITHM = "algorithm"

const val KEY_ID = "id"
const val KEY_AUTHORIZATION_ID = "authorization_id"
const val KEY_CONNECTION_ID = "connection_id"
const val KEY_STATUS = "status"
const val KEY_AUTHENTICATION_URL = "authentication_url"
const val KEY_SCA_SERVICE_URL = "sca_service_url"
const val KEY_API_VERSION = "api_version"
const val KEY_PROVIDER_ID = "provider_id"
const val KEY_PROVIDER_NAME = "provider_name"
const val KEY_PROVIDER_LOGO_URL = "provider_logo_url"
const val KEY_PROVIDER_SUPPORT_EMAIL = "provider_support_email"
const val KEY_GEOLOCATION_REQUIRED = "geolocation_required"
const val KEY_PROVIDER_PUBLIC_KEY = "provider_public_key"
const val KEY_PUBLIC_KEY = "public_key"
const val KEY_SUCCESS = "success"
const val KEY_CONFIRM = "confirm"
const val KEY_PROVIDER_CODE = "provider_code"
const val KEY_CONNECT_URL = "connect_url"

const val KEY_ACCESS_TOKEN = "access_token"
const val KEY_AUTHORIZATION_CODE = "authorization_code"
const val KEY_CREATED_AT = "created_at"
const val KEY_UPDATED_AT = "updated_at"
const val KEY_TITLE = "title"
const val KEY_DESCRIPTION = "description"
const val KEY_EXPIRES_AT = "expires_at"
const val KEY_GEOLOCATION = "geolocation"
const val KEY_USER_AUTHORIZATION_TYPE = "user_authorization_type"
const val KEY_ENC_RSA_PUBLIC = "enc_rsa_public_key"

const val KEY_CODE = "code"
const val KEY_NAME = "name"
const val KEY_ACCOUNT_NUMBER = "account_number"
const val KEY_SORT_CODE = "sort_code"
const val KEY_IBAN = "iban"
const val KEY_USER_ID = "user_id"
const val KEY_CONSENT_ID = "consent_id"
const val KEY_TPP_NAME = "tpp_name"
const val KEY_ACCOUNTS = "accounts"
const val KEY_SHARED_DATA = "shared_data"
const val KEY_CONSENT_TYPE = "consent_type"
const val KEY_BALANCE = "balance"
const val KEY_TRANSACTIONS = "transactions"
const val KEY_LOGO_URL = "logo_url"
const val KEY_VERSION = "version"
const val KEY_SUPPORT_EMAIL = "support_email"
const val KEY_CONSENT_MANAGEMENT = "consent_management"

const val KEY_ACTION_ID = "action_id"

// Error classes
const val ERROR_CLASS_AUTHENTICATION_RESPONSE = "AuthenticationError"
const val ERROR_CLASS_API_RESPONSE = "ApiResponseError"
const val ERROR_CLASS_API_REQUEST = "ApiRequestError"
const val ERROR_CLASS_HOST_UNREACHABLE = "HostUnreachable"
const val ERROR_CLASS_SSL_HANDSHAKE = "SSLHandshakeException"
const val ERROR_CLASS_CONNECTION_NOT_FOUND = "ConnectionNotFound"
const val ERROR_CLASS_AUTHORIZATION_NOT_FOUND = "AuthorizationNotFound"
const val ERROR_CLASS_INVALID_DEEPLINK = "InvalidDeeplink"
