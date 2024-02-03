package org.cec.brick.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import org.cec.brick.engine.BrickEngine
import org.cec.brick.engine.journal.JournalSubsystem
import org.cec.brick.screens.DebugScreen
import org.cec.brick.screens.SettingsScreen
import org.cec.brick.ui.CecColors
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import java.nio.file.Paths

@Composable
fun App() {
    Paths.get("settings.json")
    MaterialTheme(
        colorScheme = CecColors(),
    ) {
        KoinContext {
            val brick = koinInject<BrickEngine>()
            val scope = rememberCoroutineScope()
            val job = scope.launch { brick.start() }
            var show by remember { mutableStateOf(false) }
            val selected = remember { mutableStateOf(0) }
            LaunchedEffect(Unit) {
                job.join()
                show = true
            }
            Row {
                Column(horizontalAlignment = Alignment.Start) {
                    Navigation(selected)
                }
                if (show) when (selected.value) {
                    0 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        DebugScreen()
                    }

                    1 -> Column { SettingsScreen() }
                    2 -> Unit
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

@Composable
fun Navigation(selected: MutableState<Int>) {
    val items = listOf("Home", "Settings", "Log")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Settings, Icons.Filled.Search)
    NavigationRail {
        items.forEachIndexed { index, item ->
            NavigationRailItem(
                icon = { Icon(icons[index], contentDescription = item) },
                label = { Text(item) },
                selected = selected.value == index,
                onClick = { selected.value = index }
            )
        }
    }
}
