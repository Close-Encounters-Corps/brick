package org.cec.brick.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import org.cec.brick.subsystem.journal.JournalSubsystem
import org.cec.brick.ui.CecColors
import org.cec.brick.ui.CecShapes
import org.cec.brick.ui.CecTypography

@Composable
fun App() {
    MaterialTheme(
        colors = CecColors(),
        typography = CecTypography,
        shapes = CecShapes
    ) {
//        val tabIndex by remember { mutableStateOf(0) }
//        val tabs = listOf("Home", "Settings", "Diagnostics")
//        val scope = rememberCoroutineScope()
        var journals by remember { mutableStateOf<JournalSubsystem?>(null) }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                journals = Subsystems.Journals
            }) {
                Text("press me!")
            }
            journals?.let { LogScreen(it) }
        }
    }
}

@Composable
fun LogScreen(subsystem: JournalSubsystem) {
    val lines = remember { mutableStateListOf("") }
    LazyColumn(state = rememberLazyListState()) {
        items(lines.size) {
            Text(lines[it])
        }
    }
    LaunchedEffect(subsystem) {
        subsystem.lines()
            .map {
                lines.add(it)
            }.collect()
    }
}

object Subsystems {
    val Journals = JournalSubsystem()
}
