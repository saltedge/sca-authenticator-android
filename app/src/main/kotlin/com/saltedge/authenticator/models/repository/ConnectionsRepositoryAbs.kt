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
package com.saltedge.authenticator.models.repository

import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.sdk.model.GUID
import com.saltedge.authenticator.sdk.model.Token

/**
 * Abstraction of ConnectionsRepository
 * @see ConnectionsRepository
 */
interface ConnectionsRepositoryAbs {
    fun isEmpty(): Boolean
    fun getConnectionsCount(): Long
    fun getConnectionsCount(providerCode: String): Long
    fun hasActiveConnections(): Boolean
    fun connectionExists(connection: Connection): Boolean
    fun connectionExists(connectionGuid: GUID?): Boolean
    fun getAllConnections(): List<Connection>
    fun getAllActiveConnections(): List<Connection>
    fun getByGuid(connectionGuid: GUID?): Connection?
    fun getById(connectionId: String): Connection?
    fun getByConnectUrl(connectionUrl: String): List<Connection>
    fun deleteAllConnections()
    fun deleteConnection(connectionGuid: GUID): Boolean
    fun invalidateConnectionsByTokens(accessTokens: List<Token>)
    fun saveModel(connection: Connection): Connection?
    fun fixNameAndSave(connection: Connection)
    fun updateNameAndSave(connection: Connection, newName: String)
}
