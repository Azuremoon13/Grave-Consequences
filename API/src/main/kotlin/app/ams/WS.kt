package app.ams

import app.ams.dataClasses.Config
import app.ams.dataClasses.HandShake
import app.ams.dataClasses.Payload
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds
import java.util.Collections
import java.util.LinkedHashSet

fun Application.configureSockets() {
    val config: Config = this.configData
    val WSConnections = Collections.synchronizedSet(LinkedHashSet<Connection>())

    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        get("/") {
            call.respond(
                WebSocketUpgrade(call) {
                    val clientIP = call.request.origin.remoteAddress
                    val WS = Connection(this)
                    WSConnections += WS
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val payload = frame.readText()
                            try {
                                if (!WS.initialized){
                                    GCLogger.debug("Received from WS: $payload")
                                    val handshake = WS.init(Json.decodeFromString<HandShake>(payload))
                                    this.send(Frame.Text(handshake.response))
                                    if (handshake.response != "success"){
                                        WSConnections -= WS
                                        GCLogger.info("Incoming connection from: $clientIP - ${WS.name} not authenticated: ${handshake.response}")
                                    } else {
                                        GCLogger.info("Incoming connection from: $clientIP - ${WS.name} approved")
                                    }
                                } else {
                                    var WSCounter = 0
                                    WSConnections.forEach { ws ->
                                        if ((ws.clusterKey == WS.clusterKey || !config.separateByClusterKey) && isValidPayload(payload))
                                            ws.session.send(Frame.Text(payload))
                                            GCLogger.debug("Sent ${ws.name} payload of: $payload")
                                            WSCounter += 1
                                    }
                                    if (WSCounter > 0) {
                                        GCLogger.info("Sent payload to $WSCounter client(s)")
                                        WSCounter = 0
                                    }
                                }
                            } catch (e: Exception){
                                GCLogger.error(e.toString())
                            }
                        }
                    }
                    GCLogger.info("Connection Closed to ${WS.name}")
                    WSConnections -= WS
                }
            )

        }
    }
}

fun isValidPayload (payload: String): Boolean {
    return try {
        Json.decodeFromString<Payload>(payload)
        true
    }
    catch (e: Exception){
        GCLogger.error(e.toString())
        false
    }
}



