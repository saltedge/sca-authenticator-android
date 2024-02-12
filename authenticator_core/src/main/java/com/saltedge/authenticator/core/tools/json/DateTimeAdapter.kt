/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.tools.json

import com.google.gson.*
import com.saltedge.authenticator.core.tools.parseToUtcDateTime
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.lang.reflect.Type

/**
 * Custom GSON Deserializer/Serializer for DateTime class.
 * Deserializer parse json string (from JsonElement) to DateTime with UTC DateTimeZone.
 * Serializer creates JsonElement from DateTime source
 */
class DateTimeAdapter : JsonDeserializer<DateTime>, JsonSerializer<DateTime> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): DateTime? {
        return try {
            json.asString.parseToUtcDateTime() ?: DateTime(0).withZone(DateTimeZone.UTC)
        } catch (e: Exception) {
            DateTime(0).withZone(DateTimeZone.UTC)
        }
    }

    override fun serialize(
        src: DateTime,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src.withZone(DateTimeZone.UTC).toString())
    }
}
