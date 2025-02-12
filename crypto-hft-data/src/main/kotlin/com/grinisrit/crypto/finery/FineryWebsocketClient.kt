package com.grinisrit.crypto.finery

import com.grinisrit.crypto.FineryPlatform
import com.grinisrit.crypto.common.websocket.SeveralRequestWebsocketClient
import com.grinisrit.crypto.common.websocket.SingleRequestWebsocketClient
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.io.File


fun FineryPlatform.createFinerySource(requests: List<String>): FineryWebsocketClient {
    return FineryWebsocketClient(this, requests)
}

class FineryWebsocketClient
internal constructor(
    platform: FineryPlatform,
    requests: List<String>,
) : SeveralRequestWebsocketClient(
    platform,
    requests,
    backendReconnectTimeout = 4000L,
    socketTimeoutMillis = 4000L,
    aliveBound = 10000,
) {
    private val key = File(platform.keyPath).readText()
    private val secret = File(platform.secretPath).readText()

    override suspend fun DefaultClientWebSocketSession.authorize() {
        val authRequest = getAuthRequest(key, secret)
        send(Frame.Text(authRequest))
        delay(2000L) // TODO
    }

}