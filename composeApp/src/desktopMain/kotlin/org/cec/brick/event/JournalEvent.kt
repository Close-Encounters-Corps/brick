package org.cec.brick.event

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import org.cec.brick.api.Brick
import org.cec.brick.util.ZonedDateTimeSerializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDateTime
import java.time.ZonedDateTime

class JournalEvent(
    val name: String,
    val ts: LocalDateTime,
    val body: JsonObject
) : KoinComponent {
    val brick: Brick by inject()

    @Serializable
    private data class Simple(
        val event: String,
        @Serializable(with = ZonedDateTimeSerializer::class)
        val timestamp: ZonedDateTime
    )

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun parse(str: String): JournalEvent {
            val obj = json.decodeFromString<JsonObject>(str)
            val decoded = json.decodeFromJsonElement<Simple>(obj)
            return JournalEvent(decoded.event, decoded.timestamp.toLocalDateTime(), body = obj)
        }
    }
}
