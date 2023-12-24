package org.cec.brick.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.cec.brick.api.Brick
import org.cec.brick.api.BrickPlugin
import org.cec.brick.event.BrickEvent
import org.cec.brick.plugins.JournalBuffer
import org.cec.brick.plugins.StatisticsPlugin
import org.cec.brick.subsystem.journal.JournalSubsystem
import org.cec.brick.subsystem.journal.JournalsKey

/**
 * Default implementation of public [Brick] API
 * @author Igor Ovsyannikov
 */
class BrickEngine : Brick {
    var running = false
    override val attributes = Attributes()
    private val pipeline = PipelineEngine()
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val plugins = listOf(StatisticsPlugin(), JournalBuffer()).associateBy { it.name }

    fun collectPlugins(): Collection<BrickPlugin> {
        return plugins.values
    }

    override suspend fun emit(event: BrickEvent) {
        pipeline.send(event)
    }

    suspend fun start() {
        if (running) return
        running = true
        val plugins = collectPlugins()
        plugins.forEach { plugin ->
            plugin.install(pipeline)
            plugin.key?.let { attributes.put(it, plugin) }
        }
        val journals = JournalSubsystem(this@BrickEngine)
        attributes.put(JournalsKey, journals)
        launch(journals)
    }

    suspend fun launch(journals: JournalSubsystem) = scope.launch {
        journals.events()
            .onEach { pipeline.send(it) }
            .catch {
                // rethrow any runtime errors
                if (it !is Exception) throw it
                it.printStackTrace()
            }.collect()
    }
}