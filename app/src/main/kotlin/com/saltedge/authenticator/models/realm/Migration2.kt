/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.models.realm

import io.realm.RealmSchema

/**
 * Add the supportEmail field to the Connection model
 */
fun RealmSchema.runMigration2() {
    get("Connection")?.addField("supportEmail", String::class.java)
}
