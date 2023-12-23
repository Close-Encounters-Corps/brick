package org.cec.brick.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.cec.brick.subsystem.journal.JournalSubsystem
import java.nio.file.NoSuchFileException

@Composable
fun LogScreen(subsystem: JournalSubsystem) {
    var error by remember { mutableStateOf<String?>(null) }
    val items = remember { mutableStateMapOf<String, Long>() }
    error?.let { Text(it) }
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
            error = "journal not found: ${exc.message}"
        }
    }
}
