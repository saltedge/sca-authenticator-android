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
package com.saltedge.authenticator.models.realm

import android.content.Context
import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.core.tools.createRandomGuid
import com.saltedge.authenticator.models.repository.PreferenceRepository
import io.realm.Realm
import io.realm.RealmConfiguration

const val DB_SCHEMA_VERSION = 4L

interface RealmManagerAbs {
    val initialized: Boolean
    val errorOccurred: Boolean
    fun initRealm(context: Context)
    fun getDefaultInstance(): Realm
    fun resetError()
}

object RealmManager : RealmManagerAbs {

    private const val DB_NAME = "authenticator_app.realm"
    private var initErrorOccurred: Boolean? = null
    override val initialized: Boolean
        get() = initErrorOccurred != null
    override val errorOccurred: Boolean
        get() = initErrorOccurred == true

    override fun initRealm(context: Context) {
        if (!initialized) {
            Realm.init(context)
            val builder = RealmConfiguration.Builder()
                .schemaVersion(DB_SCHEMA_VERSION)
                .name(DB_NAME)
            builder.migration(runMigrations())
            if (AppTools.isTestsSuite(context)) builder.inMemory()
            else builder.encryptionKey(getOrCreateDatabaseKey())
            Realm.setDefaultConfiguration(builder.build())
            checkDbInstance()
        }
    }

    override fun getDefaultInstance(): Realm = Realm.getDefaultInstance()

    override fun resetError() {
        initErrorOccurred = null
    }

    /**
     * Get or create new database key
     *
     * @return database key
     */
    private fun getOrCreateDatabaseKey(): ByteArray {
        if (PreferenceRepository.dbKey.isEmpty()) PreferenceRepository.dbKey = createRandomGuid()
        return PreferenceRepository.dbKey.toByteArray()
    }

    private fun checkDbInstance() {
        try {
            initErrorOccurred = false
            Realm.getDefaultInstance()
        } catch (e: Exception) {
            e.printStackTrace()
            initErrorOccurred = true
        }
    }
}
