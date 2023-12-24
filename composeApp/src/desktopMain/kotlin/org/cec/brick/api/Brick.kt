package org.cec.brick.api

import org.cec.brick.engine.Attributes
import org.cec.brick.event.BrickEvent

interface Brick {
    val attributes: Attributes
    suspend fun emit(event: BrickEvent)
}