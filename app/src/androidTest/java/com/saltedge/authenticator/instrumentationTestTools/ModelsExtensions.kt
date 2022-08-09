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
