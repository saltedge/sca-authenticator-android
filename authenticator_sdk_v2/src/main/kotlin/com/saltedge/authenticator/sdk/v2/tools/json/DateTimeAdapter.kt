/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
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
package com.saltedge.authenticator.sdk.v2.tools.json

import com.google.gson.*
import com.saltedge.authenticator.sdk.v2.tools.parseToUtcDateTime
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.lang.reflect.Type

/**
 * Custom GSON Deserializer/Serializer for DateTime class.
 * Deserializer parse json string (from JsonElement) to DateTime with UTC DateTimeZone.
 * Serializer creates JsonElement from DateTime source
 */
internal class DateTimeAdapter : JsonDeserializer<DateTime>, JsonSerializer<DateTime> {

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
