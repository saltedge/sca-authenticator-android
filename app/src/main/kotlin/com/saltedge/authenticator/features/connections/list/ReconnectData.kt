/*
 * Copyright (c) 2022 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.connections.list

import com.saltedge.authenticator.core.model.GUID

data class ReconnectData(val guid: GUID, val apiVersion: String)
