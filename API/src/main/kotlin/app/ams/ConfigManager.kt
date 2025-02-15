package app.ams

import app.ams.dataClasses.Config
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File

private val json = Json {
    encodeDefaults = true
    prettyPrint = true
}

object ConfigManager {

    private val _config: Config by lazy {

        val configFile = File("config.json")
        if (!configFile.exists() || configFile.readText().isBlank()) {
            val defaultConfig = Config()
            GCLogger.info("Created default configuration file")
            configFile.writeText(json.encodeToString(defaultConfig))
            defaultConfig
        } else {
            GCLogger.info("Loading default configuration file")
            Json.decodeFromString(configFile.readText())
        }
    }

    val config: Config
        get() = _config
}
