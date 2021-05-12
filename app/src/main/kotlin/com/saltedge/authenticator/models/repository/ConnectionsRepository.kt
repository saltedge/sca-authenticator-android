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

import com.saltedge.authenticator.app.*
import com.saltedge.authenticator.core.api.KEY_ID
import com.saltedge.authenticator.core.api.KEY_STATUS
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.core.model.Token
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.realm.RealmManager
import io.realm.Realm
import io.realm.RealmQuery
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

object ConnectionsRepository : ConnectionsRepositoryAbs {

    /**
     * Checks if the database doesn't contains a connections
     *
     * @return boolean, true if the number of connections is zero
     */
    override fun isEmpty(): Boolean {
        return RealmManager.getDefaultInstance().use {
            it.where(Connection::class.java).count() == 0L
        }
    }

    /**
     * Get count of all connections in database
     *
     * @return the count of connections
     */
    override fun getConnectionsCount(): Long {
        return RealmManager.getDefaultInstance().use { realm ->
            realm.where(Connection::class.java).count()
        }
    }

    /**
     * Get count of all connections in database by provider providerCode
     *
     * @param providerCode - providerCode of Connection
     * @return the count of connections
     */
    override fun getConnectionsCount(providerCode: String): Long {
        return RealmManager.getDefaultInstance().use { realm ->
            realm.where(Connection::class.java).equalTo(KEY_CODE, providerCode).count()
        }
    }

    /**
     * Check if valid connections contains in database
     *
     * @return boolean, true if count of valid connections is more than 0
     * @see createActiveConnectionsQuery
     */
    override fun hasActiveConnections(): Boolean {
        return RealmManager.getDefaultInstance().use {
            it.createActiveConnectionsQuery().count() > 0L
        }
    }

    /**
     * Get all connections from database, sorted by creation date
     *
     * @return list of connections
     */
    override fun getAllConnections(): List<Connection> {
        return RealmManager.getDefaultInstance().use {
            it.where(Connection::class.java).sort(KEY_CREATED_AT).findAll()
        }
    }

    /**
     * Get all valid connections from database
     *
     * @return list of connections
     * @see createActiveConnectionsQuery
     */
    override fun getAllActiveConnections(): List<Connection> {
        return RealmManager.getDefaultInstance().use {
            it.copyFromRealm(it.createActiveConnectionsQuery().findAll())
        }
    }

    /**
     * Get Connections by connection url
     *
     * @param connectionUrl - connection url of Connection
     * @return Connection by url
     */
    override fun getByConnectUrl(connectionUrl: String): List<Connection> {
        return RealmManager.getDefaultInstance().use { realmDb ->
            realmDb.where(Connection::class.java)
                .equalTo(DB_KEY_CONNECT_URL, connectionUrl)
                .equalTo(KEY_STATUS, ConnectionStatus.ACTIVE.toString())
                .sort(KEY_CREATED_AT)
                .findAll().map {
                realmDb.copyFromRealm(it)
            }
        }
    }

    /**
     * Delete all connections from database
     */
    override fun deleteAllConnections() {
        RealmManager.getDefaultInstance().use {
            it.executeTransaction { realmDb -> realmDb.delete(Connection::class.java) }
        }
    }

    /**
     * Delete connection from database by guid
     *
     * @param connectionGuid - connection guid
     * @return boolean, false if guid is empty or raised exception while saving in db
     */
    override fun deleteConnection(connectionGuid: GUID): Boolean {
        if (connectionGuid.isEmpty() || !connectionExists(connectionGuid)) return false
        RealmManager.getDefaultInstance().use {
            it.executeTransaction { realmDb ->
                realmDb.where(Connection::class.java).equalTo(KEY_GUID, connectionGuid)
                    .findAll().deleteAllFromRealm()
            }
        }
        return true
    }

    /**
     * Invalidate connections by accessTokens.
     * Updates the status for each connection to ConnectionStatus.INACTIVE
     * and accessToken to empty string
     *
     * @param accessTokens - list of access tokens
     */
    override fun invalidateConnectionsByTokens(accessTokens: List<Token>) {
        RealmManager.getDefaultInstance().use {
            it.executeTransaction { realmDb ->
                realmDb.where(Connection::class.java)
                    .`in`(KEY_ACCESS_TOKEN, accessTokens.toTypedArray())
                    .findAll()
                    .forEach { model ->
                        model.status = ConnectionStatus.INACTIVE.toString()
                        model.accessToken = ""
                    }
            }
        }
    }

    /**
     * Check if connection with specific guid exist in db
     *
     * @param connection - Connection model
     * @return boolean, true if connection exists
     */
    override fun connectionExists(connection: Connection): Boolean =
        connectionExists(connection.guid)

    /**
     * Check if connection with specific guid exist in db
     *
     * @param connectionGuid - guid of Connection model
     * @return boolean, true if connection exist
     * @see getByGuid
     */
    override fun connectionExists(connectionGuid: GUID?): Boolean =
        getByGuid(connectionGuid) != null

    /**
     * Get Connection by Guid
     *
     * @param connectionGuid - guid (optional) of Connection
     * @return Connection with a specific guid
     */
    override fun getByGuid(connectionGuid: GUID?): Connection? {
        return RealmManager.getDefaultInstance().use { realmDb ->
            if (connectionGuid.isNullOrEmpty()) null else {
                realmDb.where(Connection::class.java).equalTo(
                    KEY_GUID,
                    connectionGuid
                ).findFirst()?.let {
                    realmDb.copyFromRealm(it)
                }
            }
        }
    }

    /**
     * Get Connection by Id
     *
     * @param connectionId - id of Connection
     * @return Connection by id
     */
    override fun getById(connectionId: String): Connection? {
        return RealmManager.getDefaultInstance().use { realmDb ->
            realmDb.where(Connection::class.java).equalTo(KEY_ID, connectionId).findFirst()?.let {
                realmDb.copyFromRealm(it)
            }
        }
    }

    /**
     * Save model of Connection
     *
     * @param connection - model of Connection
     * @return saved Connection
     */
    override fun saveModel(connection: Connection): Connection? {
        if (connection.createdAt == 0L) connection.createdAt = DateTime.now().withZone(DateTimeZone.UTC).millis
        connection.updatedAt = DateTime.now().withZone(DateTimeZone.UTC).millis
        var result: Connection? = null
        RealmManager.getDefaultInstance().use {
            it.executeTransaction { realmDb ->
                result = realmDb.copyFromRealm(realmDb.copyToRealmOrUpdate(connection))
            }
        }
        return result
    }

    /**
     * Check by connection code count of with same code and guid is null.
     * Then we add to the connection name add one more value.
     *
     * @param connection - model of Connection
     * @see saveModel
     */
    override fun fixNameAndSave(connection: Connection) {
        getConnectionsCount(connection.code).let {
            if (it > 0L) connection.name = "${connection.name} (${it + 1})"
        }
        saveModel(connection)
    }

    /**
     * Update name of connection and save the model
     *
     * @param connection - model of Connection
     * @param newName - new name of Connection
     * @see saveModel
     */
    override fun updateNameAndSave(connection: Connection, newName: String) {
        connection.name = newName
        saveModel(connection)
    }

    /**
     * Creates connection query
     *
     * @receiver realm instance
     * @return RealmQuery object with conditions: Connection.status equal to ConnectionStatus.ACTIVE,
     * Connection.accessToke is not empty and result is sorted by creation date
     */
    private fun Realm.createActiveConnectionsQuery(): RealmQuery<Connection> {
        return this.where(Connection::class.java)
            .equalTo(KEY_STATUS, ConnectionStatus.ACTIVE.toString())
            .notEqualTo(KEY_ACCESS_TOKEN, "")
            .sort(KEY_CREATED_AT)
    }
}
