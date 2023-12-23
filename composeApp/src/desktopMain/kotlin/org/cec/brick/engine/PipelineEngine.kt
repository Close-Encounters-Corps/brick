package org.cec.brick.engine

import org.cec.brick.api.Brick
import org.cec.brick.event.BrickEvent
import org.cec.brick.event.JournalEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class PipelineEngine : KoinComponent {
    public val brick by inject<Brick>()
    private val hooks = mutableListOf<JournalHook>()
    private val interceptors = mutableListOf<BrickEventHook>()

    fun onEvent(name: String, hook: PipelineCall<BrickEvent>.() -> Unit) {
        interceptors.add(BrickEventHook(name, hook))
    }

    fun listen(vararg names: String, block: PipelineCall<JournalEvent>.() -> Unit) {
        names.forEach { name ->
            hooks.add(JournalHook(name, block))
        }
    }

    internal fun send(event: JournalEvent) {
        val call = PipelineCall(event)
        hooks.forEach {
            when (it.name) {
                "*" -> it.block(call)
                event.name -> it.block(call)
            }
        }
    }

    class BrickEventHook(val name: String, val block: PipelineCall<BrickEvent>.() -> Unit)
    class JournalHook(val name: String, val block: PipelineCall<JournalEvent>.() -> Unit)
}

