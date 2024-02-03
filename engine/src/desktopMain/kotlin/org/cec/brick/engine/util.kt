package org.cec.brick.engine

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.cec.brick.engine.event.JournalEvent

public inline fun <reified T : Any> JournalEvent.receive(): T {
    return Json.decodeFromJsonElement<T>(body)
}