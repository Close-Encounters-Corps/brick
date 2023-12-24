package org.cec.brick.api

import org.cec.brick.engine.AttributeKey
import org.cec.brick.engine.PipelineEngine

/**
 * Plugin specification.
 *
 */
abstract class BrickPlugin(
    val name: String,
    val key: AttributeKey<out BrickPlugin>? = null
) {
    abstract fun install(engine: PipelineEngine)
}
