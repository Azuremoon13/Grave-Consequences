package app.ams

import app.ams.dataClasses.Config
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

import io.ktor.server.application.*
import io.ktor.util.*

private val ConfigKey = AttributeKey<Config>("config")

var Application.configData: Config
    get() = attributes[ConfigKey]
    set(value) { attributes.put(ConfigKey, value) }


fun main(args: Array<String>) {
    val config = ConfigManager.config
    val port = config.port

    GCLogger.info("Starting API on port: $port")

    embeddedServer(Netty, port = port) {
        module(config)
    }.start(wait = true)
}

fun Application.module(config: Config) {
    this.configData = config
    GCLogger.init(config)

    connectDatabases()
    TokenService()
    configureSockets()
}
