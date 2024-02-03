package org.cec.brick.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.File

class Configuration(
    val path: File
) {
    private val serializer = Json {
        prettyPrint = true
    }

    private var _settings = if (path.exists()) {
        serializer.decodeFromString(path.readText())
    } else defaultSettings()

    val settings get() = _settings
    val plugins get() = _settings.plugins

    fun plugin(name: String): PluginConfiguration {
        return plugins[name] ?: PluginConfiguration(settings = JsonObject(mapOf()), enabled = false)
    }

    fun updatePlugin(name: String, settings: PluginConfiguration) {
        _settings.plugins[name] = settings
    }

    fun save() {
        path.writeText(serializer.encodeToString(_settings))
    }

    fun defaultSettings(): Settings {
        return Settings()
    }
}

@Serializable
data class Settings(
    val plugins: MutableMap<String, PluginConfiguration> = mutableMapOf()
)

@Serializable
data class PluginConfiguration(
    val settings: JsonObject,
    val enabled: Boolean
)
