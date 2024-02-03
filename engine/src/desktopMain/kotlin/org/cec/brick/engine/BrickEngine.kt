package org.cec.brick.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.cec.brick.engine.api.Brick
import org.cec.brick.engine.api.BrickPlugin
import org.cec.brick.engine.event.BrickEvent
import org.cec.brick.engine.journal.JournalSubsystem
import org.cec.brick.engine.journal.JournalsKey

/**
 * Default implementation of a public [Brick] API
 * @author Igor Ovsyannikov
 */
class BrickEngine(
    plugins: List<String>,
) : Brick {
    //    val log = Logger
    var running = false
    override val attributes = Attributes()
    private val pipeline = PipelineEngine()
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val plugins: List<BrickPlugin> = plugins.map {
        val result = Class.forName(it).constructors[0]?.newInstance() as? BrickPlugin
            ?: error("cannot find plugin $it")
        result
    }

    override suspend fun emit(event: BrickEvent) {
        pipeline.send(event)
    }

    suspend fun start() {
        if (running) return
        running = true
        val journals = JournalSubsystem(this@BrickEngine)
        attributes.put(JournalsKey, journals)
        plugins.forEach { plugin ->
            plugin.install(pipeline)
            plugin.key?.let { attributes.put(it, plugin) }
        }
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