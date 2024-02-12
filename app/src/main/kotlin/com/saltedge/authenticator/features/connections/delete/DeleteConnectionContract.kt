/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.connections.delete

import com.saltedge.authenticator.core.model.GUID

interface DeleteConnectionContract {

    interface View {
        fun dismissView()
        fun returnSuccessResult(guid: GUID)
    }
}
