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

import io.realm.RealmMigration
import io.realm.RealmSchema

/**
 * running db migrations. migrations starts from 2 index
 */
fun runMigrations(): RealmMigration {
    return RealmMigration { realm, oldVersion, newVersion ->
        val realmSchema: RealmSchema = realm.schema
        for (i in (oldVersion + 1)..newVersion) {
            when (i) {
                2L -> realmSchema.runMigration2()
                3L -> realmSchema.runMigration3()
                4L -> realmSchema.runMigration4()
                // Here to add future migrations
                // `XX -> realmSchema.runMigrationXX()`
                // Where `XX` number of schema version
            }
        }
    }
}
