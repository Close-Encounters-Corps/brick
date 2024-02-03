package org.cec.brick.engine.journal

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.cec.brick.engine.BrickEngine
import org.cec.brick.engine.attributeKeyOf
import org.cec.brick.engine.event.BrickEvent
import org.cec.brick.engine.event.JournalEvent
import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

val JournalsKey = attributeKeyOf<JournalSubsystem>("JournalSubsystem")

class JournalSubsystem(val brick: BrickEngine) {

    val journalRegex = Regex("""Journal\.(?<ts>[^\.]+)\.(?<num>\d+).log""")

    val journalDateFormat = DateTimeFormatterBuilder()
        .parseCaseSensitive()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral('T')
        .appendValue(ChronoField.HOUR_OF_DAY, 2)
        .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
        .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
        .toFormatter()

    val path = AtomicReference(
        Path(
            System.getProperty("user.home"),
            "Saved Games",
            "Frontier Developments",
            "Elite Dangerous"
        )
    )
    val offset: AtomicInteger = AtomicInteger(0)
    val journal = AtomicReference<JournalFile>(findLastJournal())

    fun findLastJournal(): JournalFile? {
        val path = path.get()
        val entries = path.listDirectoryEntries("Journal*.log")
        val out = entries.asSequence()
            .mapNotNull {
                journalRegex.find(it.name)
            }
            .map {
                val dt = LocalDateTime.parse(it.groupValues[1], journalDateFormat)
                val num = it.groupValues[2].toInt()
                JournalFile(Path(path.toString(), it.groups[0]!!.value).toFile(), dt, num)
            }.sortedBy { it.dt }.lastOrNull()
        return out
    }

    fun lastJournals(current: JournalFile? = findLastJournal()) = flow<JournalFile> {
        var last: JournalFile? = current
        last?.let { emit(it) }
        while (true) {
            delay(5000)
            val new = findLastJournal()
            new?.let {
                if (it != last) emit(it)
            }
            last = new
        }
    }

    /**
     * Wrapper around [lines] that emits parsed [JournalEvent]
     * instead of plain strings.
     * @see    lines
     * @author Igor Ovsyannikov
     */
    suspend fun events() = lines().map { JournalEvent.parse(it) }

    /**
     * Continuously get new events until the end of file.
     * If there will be a new journal, the flow will be switched to it.
     * @return flow of JSON strings
     * @author Igor Ovsyannikov
     */
    suspend fun lines(): Flow<String> {
        val journal = AtomicReference(findLastJournal())
        return channelFlow<String> {
            val emitter = launch {
                var last = journal.get()
                while (true) {
                    last?.lines()?.let {
                        it.drop(offset.get())
                            .withIndex()
                            .onEach {
                                send(it.value)
                                offset.getAndIncrement()
                            }.collect()
                    }
                    val current = journal.get()
                    if (current != last) {
                        // we've got a new journal
                        last = current
                    } else delay(1000)
                }
            }
            val updater = launch {
                lastJournals(journal.get()).onEach {
                    journal.set(it)
                    brick.emit(BrickEvent("NewJournal"))
                }.collect()
            }
            emitter.join()
            updater.join()
        }
    }

    fun set(path: Path) {
        if (path.toString() == this.path.toString()) return
        this.path.set(path)
        journal.set(findLastJournal())
        offset.set(0)
    }

    data class JournalFile(val file: File, val dt: LocalDateTime, val num: Int) {
        fun lines() = flow<String> {
            file.useLines { seq ->
                emitAll(seq.asFlow())
            }
        }
    }
}