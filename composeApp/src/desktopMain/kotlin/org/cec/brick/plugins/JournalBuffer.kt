package org.cec.brick.plugins

import org.cec.brick.api.BrickPlugin
import org.cec.brick.engine.PipelineEngine
import org.cec.brick.event.JournalEvent

class JournalBuffer(
    val limit: Int = 10000
) : BrickPlugin {
    val items = mutableListOf<JournalEvent>()
    override fun install(engine: PipelineEngine) {
        engine.listen("*") {
            while (items.size > limit) items.removeAt(0)
            items.add(event)
        }
    }
}