/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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
package com.saltedge.authenticator.features.main

import android.content.Intent
import com.saltedge.authenticator.app.KEY_DEEP_LINK
import com.saltedge.authenticator.core.api.KEY_AUTHORIZATION_ID
import com.saltedge.authenticator.core.api.KEY_CONNECTION_ID

val Intent?.connectionId: String
    get() = this?.getStringExtra(KEY_CONNECTION_ID) ?: ""

val Intent?.authorizationId: String
    get() = this?.getStringExtra(KEY_AUTHORIZATION_ID) ?: ""

val Intent?.deepLink: String
    get() = this?.getStringExtra(KEY_DEEP_LINK) ?: ""

// Data for Authorization Details Fragment
val Intent?.hasPendingAuthorizationData: Boolean
    get() = this != null && this.connectionId.isNotEmpty() && this.authorizationId.isNotEmpty()

// Data for Connect Activity
val Intent?.hasDeepLinkData: Boolean
    get() = deepLink.isNotEmpty()
