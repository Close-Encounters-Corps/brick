package org.cec.triumvirate

import androidx.compose.runtime.Composable
import org.cec.brick.engine.PipelineEngine
import org.cec.brick.engine.api.BrickPlugin
import org.cec.triumvirate.updater.UpdateSettings

class TriumvirateShim : BrickPlugin("Triumvirate") {
    override fun install(engine: PipelineEngine) {

    }

    @Composable
    override fun settings() {
        UpdateSettings()
    }
}
