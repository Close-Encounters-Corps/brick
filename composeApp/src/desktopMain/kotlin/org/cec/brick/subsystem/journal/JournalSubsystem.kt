package org.cec.brick.subsystem.journal

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class JournalSubsystem(
    path: String = "C:\\Users\\kamis\\Saved Games\\Frontier Developments\\Elite Dangerous"
) {
    val path = Path(path)

    val journalRegex = Regex("""Journal\.(?<ts>[^\.]+)\.(?<num>\d+).log""")

    fun lastJournal(): JournalFile? {
        val entries = path.listDirectoryEntries("Journal*.log")
        return entries.asSequence()
            .mapNotNull {
                println(it.name)
                journalRegex.find(it.name)
            }
            .map {
                val dt = it.groupValues[1]
                val num = it.groupValues[2]
                JournalFile(Path(path.toString(), it.groups[0]!!.value).toFile())
            }.lastOrNull()
    }

    data class JournalFile(val file: File) {
        fun lines() = flow<String> {
            file.useLines { seq ->
                emitAll(seq.asFlow())
            }
        }
    }
}