/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.model.authorization

/**
 * Check that Authorization is not expired
 *
 * @receiver authorization
 * @return boolean, true if expiresAt time is after current time
 */
fun AuthorizationData.isNotExpired(): Boolean = expiresAt.isAfterNow
