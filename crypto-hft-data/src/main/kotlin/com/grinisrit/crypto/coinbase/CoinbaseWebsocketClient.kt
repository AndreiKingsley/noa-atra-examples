package com.grinisrit.crypto.coinbase

import com.grinisrit.crypto.CoinbasePlatform
import com.grinisrit.crypto.common.DataTransport
import com.grinisrit.crypto.common.websocket.WebsocketClient
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.flow.flow
import java.time.Instant

// todo()
class CoinbaseWebsocketClient(
    platform: CoinbasePlatform,
    private val request: String
) : WebsocketClient(
    platform,
    backendReconnectTimeout = 4000L
) {
    // TODO() make this function better
    override fun DefaultClientWebSocketSession.receiveData() = flow {
        loggerFile.log("Connected successfully")
        send(Frame.Text(request))
        val subResponse = incoming.receive()
        subResponse as? Frame.Text ?: throw Exception("Invalid response")
        loggerFile.log("Request sent. Server response: ${subResponse.readText()}")

        for (frame in incoming) {
            frame as? Frame.Text ?: throw Error(frame.toString()) // TODO
            val res = DataTransport.dataStringOf(platform.platformName, Instant.now(), frame.readText())
            emit(res)
        }
    }
}