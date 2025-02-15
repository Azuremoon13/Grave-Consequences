package app.ams.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class Payload(
    val id: String,
    val restrictedUntil: Int,
    val clusterKey: String,
)