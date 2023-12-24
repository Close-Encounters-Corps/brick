package org.cec.brick.engine

import org.cec.brick.api.Brick
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


/**
 * Unit of pipeline items, you can look at it as on HTTP Request.
 * This call (request) has event (body) and [attributes].
 * You can use attributes to pass data to a different components, such as subsystems or plugins.
 *
 * @param event payload of call (i.e. JournalEvent or BrickEvent)
 * @see PipelineEngine
 * @author Igor Ovsyannikov
 */
class PipelineCall<E>(
    val event: E
) : KoinComponent {
    val brick: Brick by inject()
    val attributes = Attributes()
}

