package org.cec.brick.engine.api

import androidx.compose.runtime.Composable
import org.cec.brick.engine.AttributeKey
import org.cec.brick.engine.PipelineEngine

/**
 * Plugin specification.
 * @param name: Plugin name that will be used
 *              in diagnostic messages
 * @param key: Attribute key under which this plugin
 *              will be accessible. If null, then this plugin will be hidden from others.
 */
abstract class BrickPlugin(
    val name: String,
    val key: AttributeKey<BrickPlugin>? = null
) {
    abstract fun install(engine: PipelineEngine)

    @Composable
    open fun settings() {
    }
}
