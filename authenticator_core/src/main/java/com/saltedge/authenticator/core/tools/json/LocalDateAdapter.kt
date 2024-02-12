/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.tools.json

import com.google.gson.*
import org.joda.time.LocalDate
import java.lang.reflect.Type

/**
 * Custom GSON Deserializer/Serializer for LocalDate class.
 * Deserializer parse json string (from JsonElement) to LocalDate
 * Serializer creates JsonElement from LocalDate source
 */
class LocalDateAdapter : JsonDeserializer<LocalDate>, JsonSerializer<LocalDate> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): LocalDate {
        return try {
            LocalDate.parse(json.asString)
        } catch (e: Exception) {
            LocalDate(0)
        }
    }

    override fun serialize(src: LocalDate, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
        JsonPrimitive(src.toString())
}
