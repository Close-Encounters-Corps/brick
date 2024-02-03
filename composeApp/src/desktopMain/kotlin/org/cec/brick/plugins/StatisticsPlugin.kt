package org.cec.brick.plugins

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.cec.brick.engine.PipelineEngine
import org.cec.brick.engine.api.BrickPlugin
import org.cec.brick.engine.attributeKeyOf
import java.util.concurrent.ConcurrentHashMap

val StatisticsKey = attributeKeyOf<StatisticsPlugin>("Statistics")

class StatisticsPlugin : BrickPlugin("Statistics", StatisticsKey) {
    private val items = ConcurrentHashMap<String, Int>()
    private val lock = Mutex()

    suspend fun withItems(block: (Map<String, Int>) -> Unit) {
        lock.withLock {
            block(items)
        }
    }

    override fun install(engine: PipelineEngine) {
        engine.listen("*") {
            val num = items[event.name] ?: 0
            items[event.name] = num + 1
        }

        engine.listenEvent("NewJournal") {
            items.clear()
        }
    }
}