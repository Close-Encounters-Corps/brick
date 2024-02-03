package org.cec.brick.engine.api

import org.cec.brick.engine.Attributes
import org.cec.brick.engine.event.BrickEvent

interface Brick {
    val attributes: Attributes
    suspend fun emit(event: BrickEvent)
}