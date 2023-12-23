package org.cec.brick.api

import org.cec.brick.engine.PipelineEngine

interface BrickPlugin {
    fun install(engine: PipelineEngine)
}