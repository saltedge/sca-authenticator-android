/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.tools.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.joda.time.DateTime
import org.joda.time.LocalDate

/**
 * Init Gson with own rules for serialization and deserialization
 *
 * @return Gson object
 */
fun createDefaultGson(): Gson {
    return GsonBuilder()
        .disableHtmlEscaping()
        .registerTypeAdapter(DateTime::class.java, DateTimeAdapter())
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .create()
}

fun Any.toJsonString(): String = createDefaultGson().toJson(this)
