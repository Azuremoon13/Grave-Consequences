package app.ams

import app.ams.dataClasses.HandShake
import io.ktor.websocket.*

class Connection (
    val session: WebSocketSession
){
    companion object{
        const val PAYLOADVERSION = 2
    }

    enum class HandshakeResponse(
        val response: String
    ){
        SUCCESS("success"),
        BAD_AUTH("bad_auth"),
        FAIL_CLIENT("fail_client"),
        FAIL_SERVER("fail_server")
    }

    var initialized = false
    var clusterKey = ""
    var name = ""

    suspend fun init(handshake: HandShake): HandshakeResponse{
        initialized = true
        clusterKey = handshake.clusterKey
        name = handshake.name
        val Tokens = TokenService().getAllTokens()
        return when {
            !Tokens.contains(handshake.token) -> HandshakeResponse.BAD_AUTH
            PAYLOADVERSION > handshake.version -> HandshakeResponse.FAIL_CLIENT
            PAYLOADVERSION < handshake.version -> HandshakeResponse.FAIL_SERVER
            else -> HandshakeResponse.SUCCESS
        }
    }
}
