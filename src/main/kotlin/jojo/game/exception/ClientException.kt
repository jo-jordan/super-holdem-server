package jojo.game.exception

import org.java_websocket.WebSocket

class ClientException(conn: WebSocket?): RuntimeException() {

    override val message: String?
        get() = super.message
}