package app.ams

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*

fun Application.connectDatabases() {
    Database.connect(
        url = "jdbc:sqlite:${ConfigManager.config.dbUrl}",
        user = ConfigManager.config.dbUser,
        driver = "org.sqlite.JDBC",
        password = ConfigManager.config.dbPassword,
    )
}