package org.cec.brick.engine

import org.cec.brick.api.Brick
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PipelineCall<E>(
    val event: E
) : KoinComponent {
    val brick: Brick by inject()
    val attributes = Attributes()
}

