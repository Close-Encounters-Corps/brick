package org.cec.brick.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.cec.brick.subsystem.journal.JournalSubsystem
import java.nio.file.NoSuchFileException

@Composable
fun LogScreen(subsystem: JournalSubsystem) {
    val items = remember { mutableStateMapOf<String, Long>() }
    LazyColumn(state = rememberLazyListState()) {
        val keys = items.keys.toList()
        items(keys.size) {
            val item = items[keys[it]]
            Text("${keys[it]} $item lines")
        }
    }
    LaunchedEffect(subsystem) {
        try {
            subsystem.events().onEach {
                val num = items[it.name] ?: 0
                items[it.name] = num + 1
            }.collect()
        } catch (exc: NoSuchFileException) {
            println("journal not found: ${exc.message}")
        }
    }
}
