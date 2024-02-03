package org.cec.brick.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Switch
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.cec.brick.config.Configuration
import org.cec.brick.engine.BrickEngine
import org.koin.compose.koinInject

@Composable
fun SettingsScreen() {
    val engine = koinInject<BrickEngine>()

    val defaults = mapOf<String, @Composable () -> Unit>("General" to { GeneralSettings() })
    val tabIndex = remember { mutableStateOf(0) }
    val plugins = engine.plugins.associateBy { it.name }
    val tabs = defaults.keys.toList() + plugins.keys.toList()
    TabRow(
        selectedTabIndex = tabIndex.value
    ) {
        tabs.forEachIndexed { index, name ->
            Tab(text = { Text(name) },
                selected = tabIndex.value == index,
                onClick = { tabIndex.value = index }
            )
        }
    }
    val currentName = tabs[tabIndex.value]
    when (currentName) {
        in defaults.keys -> defaults[currentName]?.invoke()
            ?: println("$currentName settings not found")

        else -> plugins[currentName]?.settings()
            ?: println("$currentName plugin settings not found")
    }
}

@Composable
fun GeneralSettings() {
    val engine = koinInject<BrickEngine>()
    val config = koinInject<Configuration>()
    Text("Manage plugins")
    Text("(these settings will be applied after restart)")
    engine.plugins.forEach { plugin ->
        val settings = config.plugin(plugin.name)
        val enabled = remember { mutableStateOf(settings.enabled) }
        Row {
            Switch(checked = enabled.value, onCheckedChange = {
                config.updatePlugin(plugin.name, settings.copy(enabled = it))
                enabled.value = it
            })
            Text("Enable ${plugin.name}")
        }
    }
}
