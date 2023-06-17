package jojo.game

import jojo.game.constant.CommonHeaders
import jojo.game.core.Dispatcher
import jojo.game.dto.CommonData
import jojo.game.dto.ReqData
import jojo.game.utils.JacksonUtils
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class GameServer(port: Int): WebSocketServer(InetSocketAddress(port)) {

    private val logger = LoggerFactory.getLogger("GameServer")

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        if (handshake?.hasFieldValue(CommonHeaders.HEADER_PLAYER_ID) == false) {
            logger.info("{}: Not login, close.", conn?.remoteSocketAddress?.address?.hostAddress)
            conn?.close()
            return
        }

        conn?.setAttachment(
            CommonData(
                handshake?.getFieldValue(CommonHeaders.HEADER_PLAYER_ID) ?: "",
                handshake?.getFieldValue(CommonHeaders.HEADER_ROOM_ID) ?: ""
            )
        )
        logger.info("{}: Open connection.", conn?.remoteSocketAddress?.address?.hostAddress)
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        logger.info("{}: Close connection.", conn?.remoteSocketAddress?.address?.hostAddress)
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        val commonData = conn?.getAttachment<CommonData>()
        val dtoData = JacksonUtils.objectMapper.readValue(message, ReqData::class.java).apply {
            this.playerId = commonData?.playerId ?: ""
            this.roomId = commonData?.roomId ?: ""
        }
        Dispatcher.dispatch(dtoData, conn)
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        logger.error("{}: Error: {}", conn?.remoteSocketAddress?.address?.hostAddress, ex?.message)
    }

    override fun onStart() {
        logger.info("GameServer start.")
    }
}