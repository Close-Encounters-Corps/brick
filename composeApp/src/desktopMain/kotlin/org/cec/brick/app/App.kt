package org.cec.brick.app

import Greeting
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
        var lastFile by remember { mutableStateOf<JournalSubsystem.JournalFile?>(null) }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
//                val lines = Subsystems.Journals.lastJournal()?.lines()
//                    ?: flowOf("404 journal not found :P")
                lastFile = Subsystems.Journals.lastJournal()
            }) {
                Text("press me!")
            }
            lastFile?.let { LogScreen(it) }
        }
    }
}

@Composable
fun LogScreen(file: JournalSubsystem.JournalFile) {
    val scope = rememberCoroutineScope()
//    var lines = remember { mutableStateListOf<String>("") }
    var lines = remember { mutableStateListOf("") }
    LazyColumn(state = rememberLazyListState()) {
        items(lines.size) {
            Text(lines[it])
        }
    }
    LaunchedEffect(file) {
        lines.addAll(file.lines().toList())
    }
}

object Subsystems {
    val Journals = JournalSubsystem()
}
