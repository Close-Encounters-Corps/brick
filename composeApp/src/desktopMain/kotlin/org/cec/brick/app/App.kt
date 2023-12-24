package org.cec.brick.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cec.brick.engine.BrickEngine
import org.cec.brick.screens.LogScreen
import org.cec.brick.subsystem.journal.JournalSubsystem
import org.cec.brick.subsystem.journal.JournalsKey
import org.cec.brick.ui.CecColors
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import kotlin.io.path.Path

@Composable
fun App() {
    MaterialTheme(
        colorScheme = CecColors(),
    ) {
        KoinContext {
            val brick = koinInject<BrickEngine>()
            val scope = rememberCoroutineScope()
            val job = scope.launch { brick.start() }
            var path by remember { mutableStateOf("") }
            var show by remember { mutableStateOf(false) }
            var info by remember { mutableStateOf("") }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(info)
                TextField(
                    value = path,
                    onValueChange = { path = it },
                    label = { Text("Path to logs") },
                    singleLine = true
                )
                Button(onClick = {
                    scope.launch {
                        job.join()
                        val journals = brick.attributes[JournalsKey]
                        if (path.isBlank()) return@launch
                        journals.set(Path(path))
                    }
                }) {
                    Text("Update path")
                }
                LaunchedEffect(Unit) {
                    job.join()
                    show = true
                }
                if (show) LogScreen()
                scope.launch {
                    job.join()
                    val journals = brick.attributes[JournalsKey]
                    while (true) {
                        info = journals.debugInfo()
                        delay(200)
                    }
                }
            }
        }
    }
}

fun JournalSubsystem.debugInfo(): String {
    return buildString {
        append("Path is:")
        appendLine(path.get())
        append("Index is:")
        appendLine(offset.get())
    }
}
