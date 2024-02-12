/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.models.realm

import io.realm.RealmSchema

/**
 * Add the consentManagementSupported field to the Connection model
 * Add the geolocationRequired field to the Connection model
 */
fun RealmSchema.runMigration3() {
    get("Connection")?.let { schema ->
        schema
            .addField("consentManagementSupported", Boolean::class.java)
            .setNullable("consentManagementSupported", true)
            .transform { it.set("consentManagementSupported", null) }
//        schema
            .addField("geolocationRequired", Boolean::class.java)
            .setNullable("geolocationRequired", true)
            .transform { it.set("geolocationRequired", null) }
    }
}
