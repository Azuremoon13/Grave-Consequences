package app.ams.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val port: Int = 8080,
    val dbUrl: String = "GC.db",
    val dbUser: String = "root",
    val dbPassword: String = "",
    val debugMessages: Boolean = false,
    val separateByClusterKey: Boolean = true,
    val relayToSelf: Boolean = false,
    val initialTokenCount: Int = 1,
    val botToken: String = "",
    val botClientId: String = "",
)