package org.cec.brick.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cec.brick.api.Brick
import org.cec.brick.plugins.StatisticsKey
import org.koin.compose.koinInject

@Composable
fun LogScreen() {
    val engine = koinInject<Brick>()
    val scope = rememberCoroutineScope()
    val items = remember { mutableStateMapOf<String, Int>() }
    val stats = engine.attributes[StatisticsKey]
    LazyColumn {
        val keys = items.keys.toList()
        item(Unit) {
            Text("size is ${keys.size}")
        }
        items(keys.size) {
            val item = items[keys[it]]
            Text("${keys[it]} $item lines")
        }
    }
    scope.launch {
        while (true) {
            stats.withItems {
                items.clear()
                items.putAll(it)
            }
            delay(500)
        }
    }
}
