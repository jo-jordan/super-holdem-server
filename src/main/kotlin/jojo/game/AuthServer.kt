package jojo.game

import jojo.game.controller.LoginController
import jojo.game.dto.ReqData
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.net.InetSocketAddress

class AuthServer(port: Int): WebSocketServer(InetSocketAddress(port)) {

    private val logger = LoggerFactory.getLogger("AuthServer")

    private val loginController = LoginController()

    /**
     * Login
     */
    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        if (handshake?.hasFieldValue("X-Username") == true && handshake.hasFieldValue("X-Password")) {
            val username = handshake.getFieldValue("X-Username")
            val password = handshake.getFieldValue("X-Password")

            val reqData = ReqData.LoginDTO().apply {
                this.username = username
                this.password = password
            }
            loginController.login(reqData, conn)

            logger.info("{}: Open connection.", conn?.remoteSocketAddress?.address?.hostAddress)
            conn?.close()
        } else {
            logger.info("{}: Login failed, close.", conn?.remoteSocketAddress?.address?.hostAddress)
            conn?.close()
        }
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {

    }

    override fun onMessage(conn: WebSocket?, message: String?) {

    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        logger.error("Error: {}", ex?.message)
    }

    override fun onStart() {
        logger.info("AuthServer started.")
    }


}