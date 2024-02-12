/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.instrumentationTestTools

import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepository

fun Connection.setGuid(value: String): Connection = apply { guid = value }

fun Connection.setId(value: String): Connection = apply { id = value }

fun Connection.setCode(value: String): Connection = apply { code = value }

fun Connection.setName(value: String): Connection = apply { name = value }

fun Connection.setAccessToken(value: String): Connection = apply { accessToken = value }

fun Connection.setLogoUrl(value: String): Connection = apply { logoUrl = value }

fun Connection.setStatus(value: ConnectionStatus): Connection = apply { status = value.toString() }

fun Connection.setCreatedAt(value: Long): Connection = apply { createdAt = value }

fun Connection.setUpdatedAt(value: Long): Connection = apply { updatedAt = value }

fun Connection.setSupportEmail(value: String): Connection = apply { supportEmail = value }

fun Connection.save(): Connection? = ConnectionsRepository.saveModel(this)
