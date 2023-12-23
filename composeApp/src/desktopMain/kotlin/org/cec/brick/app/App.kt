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
import org.cec.brick.screens.LogScreen
import org.cec.brick.subsystem.journal.JournalSubsystem
import org.cec.brick.ui.CecColors
import org.koin.compose.KoinContext

@Composable
fun App() {
    MaterialTheme(
        colorScheme = CecColors(),
    ) {
        KoinContext {
            var journals by remember { mutableStateOf<JournalSubsystem?>(null) }
            var path by remember { mutableStateOf("") }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                TextField(
                    value = path,
                    onValueChange = { path = it },
                    label = { Text("Path to logs") },
                    singleLine = true
                )
                Button(onClick = {
                    journals = JournalSubsystem(path)
                }) {
                    Text("press me!")
                }
                journals?.let { LogScreen(it) }
            }
        }
    }
}

