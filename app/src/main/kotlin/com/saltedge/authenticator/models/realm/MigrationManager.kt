/*
 * Copyright (c) 2019 Salt Edge Inc.
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
                5L -> realmSchema.runMigration5()
                // Here to add future migrations
                // `XX -> realmSchema.runMigrationXX()`
                // Where `XX` number of schema version
            }
        }
    }
}
