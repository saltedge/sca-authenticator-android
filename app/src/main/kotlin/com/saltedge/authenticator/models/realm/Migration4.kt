/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.models.realm

import io.realm.FieldAttribute
import io.realm.RealmSchema

/**
 * Add the providerRsaPublicKeyPem field to the Connection model
 */
fun RealmSchema.runMigration4() {
    get("Connection")?.let { schema ->
        schema
            .addField("providerRsaPublicKeyPem", String::class.java, FieldAttribute.REQUIRED)
            .transform { it.set("providerRsaPublicKeyPem", "") }
            .addField("apiVersion", String::class.java, FieldAttribute.REQUIRED)
            .transform { it.set("apiVersion", "1") }
    }
}
