package org.cec.brick.subsystem.journal

import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class JournalSubsystem(
    path: String? = null
) {
    val path = path?.let { Path(it) }
        ?: Path(System.getProperty("user.home"), "Saved Games", "Frontier Developments", "Elite Dangerous")

    val journalRegex = Regex("""Journal\.(?<ts>[^\.]+)\.(?<num>\d+).log""")

    val journalDateFormat = DateTimeFormatterBuilder()
        .parseCaseSensitive()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral('T')
        .appendValue(ChronoField.HOUR_OF_DAY, 2)
        .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
        .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
        .toFormatter()

    fun findLastJournal(): JournalFile? {
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

    suspend fun lines(): Flow<String> {
        val journal = ThreadLocal.withInitial { findLastJournal() }
        return channelFlow<String> {
            val emitter = launch(coroutineContext + journal.asContextElement()) {
                while (true) {
                    val current = journal.get()
                    println(current)
                    current?.lines()?.let {
                        it.map {
                            send(it)
                        }.collect()
                    }
                    break
                }
            }
            val updater = launch(coroutineContext + journal.asContextElement()) {
                lastJournals(journal.get()).map {
                    journal.set(it)
                }.collect()
            }
            emitter.join()
            updater.join()
        }
    }

    data class JournalFile(val file: File, val dt: LocalDateTime, val num: Int) {
        fun lines() = flow<String> {
            file.useLines { seq ->
                emitAll(seq.asFlow())
            }
        }
    }
}