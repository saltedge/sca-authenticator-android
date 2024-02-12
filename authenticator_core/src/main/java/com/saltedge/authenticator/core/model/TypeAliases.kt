/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.model

typealias GUID = String
typealias Token = String
typealias ID = String

inline fun<T> T?.guard(nullClause: () -> Nothing): T {
    return this ?: nullClause()
}
