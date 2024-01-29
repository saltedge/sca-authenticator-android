/*
 * Copyright (c) 2024 Salt Edge Inc.
 */
package com.saltedge.authenticator.models.realm

import io.realm.RealmSchema

/**
 * Add the pushToken field to the Connection model
 */
fun RealmSchema.runMigration5() {
    get("Connection")?.let { schema ->
        schema
            .addField("pushToken", String::class.java)
            .setNullable("pushToken", true)
            .transform { it.set("pushToken", null) }
    }
}
