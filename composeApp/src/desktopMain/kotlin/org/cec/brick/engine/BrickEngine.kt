package org.cec.brick.engine

import org.cec.brick.api.Brick
import org.cec.brick.event.JournalEvent

class BrickEngine : Brick {
    override val attributes = Attributes()
    private val engine = PipelineEngine()

    internal fun send(event: JournalEvent) {
        engine.send(event)
    }
}