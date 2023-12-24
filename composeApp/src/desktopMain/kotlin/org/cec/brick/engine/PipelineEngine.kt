package org.cec.brick.engine

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.cec.brick.event.BrickEvent
import org.cec.brick.event.JournalEvent
import org.cec.brick.util.BrickDsl
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class PipelineEngine : KoinComponent {
    public val brick by inject<BrickEngine>()
    private val journalHooks = mutableListOf<JournalHook>()
    private val interceptors = mutableListOf<BrickEventHook>()

    @BrickDsl
    fun listenEvent(name: String, hook: suspend PipelineCall<BrickEvent>.() -> Unit) {
        interceptors.add(BrickEventHook(name, hook))
    }

    /**
     * Listen and handle journal events.
     * This method registers a new receiver for each of [names]
     * which will be called when a new journal event will be emitted.
     *
     * @param names event types to handle
     * @param block call handler
     * @author      Igor Ovsyannikov
     */
    @BrickDsl
    fun listen(vararg names: String, block: suspend PipelineCall<JournalEvent>.() -> Unit) {
        names.forEach { name ->
            val h = JournalHook(name)
            brick.scope.launch {
                for (e in h.queue) block(e)
            }
            journalHooks.add(h)
        }
    }

    internal suspend fun send(event: JournalEvent) {
        val call = PipelineCall(event)
        journalHooks.forEach {
            when (it.name) {
                "*" -> it.queue.send(call)
                event.name -> it.queue.send(call)
            }
        }
    }

    internal suspend fun send(event: BrickEvent) {
        val call = PipelineCall(event)
        interceptors.forEach {
            when (it.name) {
                "*" -> it.queue.send(call)
                event.name -> it.queue.send(call)
            }
        }
    }

    class BrickEventHook(
        val name: String,
        val block: suspend PipelineCall<BrickEvent>.() -> Unit
    ) {
        val queue = Channel<PipelineCall<BrickEvent>>(1000, onBufferOverflow = BufferOverflow.DROP_LATEST)
    }

    class JournalHook(
        val name: String,
    ) {
        val queue = Channel<PipelineCall<JournalEvent>>(10000, onBufferOverflow = BufferOverflow.DROP_LATEST)
    }
}

