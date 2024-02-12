/*
 * Copyright (c) 2020 Salt Edge Inc.
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
