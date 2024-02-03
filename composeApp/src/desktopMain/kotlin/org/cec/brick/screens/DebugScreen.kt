package org.cec.brick.screens

import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cec.brick.app.debugInfo
import org.cec.brick.engine.BrickEngine
import org.cec.brick.engine.journal.JournalsKey
import org.koin.compose.koinInject
import kotlin.io.path.Path

@Composable
fun DebugScreen() {
    val brick = koinInject<BrickEngine>()
    val scope = rememberCoroutineScope()
    var path by remember { mutableStateOf("") }
    var info by remember { mutableStateOf("") }
    Text(info)
    TextField(
        value = path,
        onValueChange = { path = it },
        label = { androidx.compose.material3.Text("Path to logs") },
        singleLine = true
    )
    Button(onClick = {
        scope.launch {
            val journals = brick.attributes[JournalsKey]
            if (path.isBlank()) return@launch
            journals.set(Path(path))
        }
    }) {
        androidx.compose.material3.Text("Update path")
    }
    LogScreen()
    val journals = brick.attributes[JournalsKey]
    scope.launch {
        while (true) {
            info = journals.debugInfo()
            delay(200)
        }
    }
}