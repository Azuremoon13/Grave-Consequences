package app.ams.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class HandShake(
    val name: String,
    val token: String,
    val version: Int,
    val clusterKey: String,
)