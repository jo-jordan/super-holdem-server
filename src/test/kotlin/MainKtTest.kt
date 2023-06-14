import jojo.game.AuthServer
import jojo.game.GameServer
import jojo.game.constant.CommonHeaders
import jojo.game.dto.ReqData
import jojo.game.dto.RespData
import jojo.game.entity.Player
import jojo.game.entity.Room
import jojo.game.state.PlayerState
import jojo.game.state.RoomState
import jojo.game.utils.JacksonUtils
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.CountDownLatch

class MainKtTest {

    var client: WebSocketClient? = null

    private val logger = LoggerFactory.getLogger("MainKtTest")

    private var loginResult: RespData.LoginDTO = RespData.LoginDTO()

    companion object {
        @JvmStatic
        @BeforeAll
        fun start_server() {
            val authPort = 8887
            val gamePort = 8888

            val authServer = AuthServer(authPort)
            val gameServer = GameServer(gamePort)

            authServer.start()
            gameServer.start()
        }
    }

    private fun sendAndGetResult() {
        val countDownLatch = CountDownLatch(1)
        var result: RespData? = null
        val gameClient: WebSocketClient = object : WebSocketClient(
            URI.create("ws://127.0.0.1:8888"),
            mapOf(Pair(CommonHeaders.HEADER_PLAYER_ID, loginResult.playerId))) {

            override fun onOpen(handshakedata: ServerHandshake?) {
                logger.info("Game server onOpen")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                logger.info("Game server onClose")
            }

            override fun onMessage(message: String?) {
                logger.info("Game server onMessage")
                countDownLatch.countDown()

            }

            override fun onError(ex: Exception?) {
                logger.error("Game server onError")
            }
        }


        gameClient.connect()
        countDownLatch.await()
    }


    @Test
    @Order(1)
    fun login_should_work() {

        val countDownLatch = CountDownLatch(1)
        val headers = mapOf<String, String>(
            Pair(CommonHeaders.HEADER_USERNAME, "test"),
            Pair(CommonHeaders.HEADER_PASSWORD, "test"))
        val client = object : WebSocketClient(URI.create("ws://127.0.0.1:8887"), headers) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                logger.info("onOpen")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                logger.info("onClose")
            }

            override fun onMessage(message: String?) {
                logger.info("onMessage")
                loginResult = JacksonUtils.objectMapper.readValue(message ?: "", RespData.LoginDTO::class.java)
                countDownLatch.countDown()
            }

            override fun onError(ex: Exception?) {
                logger.info("onError")
            }
        }

        client.connect()
        countDownLatch.await()


        logger.info("loginResult: $loginResult")
        assertTrue(loginResult.username == "test")

    }

    @Test
    fun player_create_room_should_work() {
//        gameClient?.send(JacksonUtils.objectMapper.writeValueAsString(
//            ReqData.RoomCreateDTO().apply { this.name = "testRoom" }
//        ))
    }

    @Test
    fun player_enter_room_should_ready() {
        val room = Room()
        room.transitionTo(RoomState.RoomInitState(room))

        val player = Player()
        room.addPlayer(player)

        assertEquals(player.room?.id, room.id)

        player.transitionTo(PlayerState.PlayerEnterRoomState(player))

        player.transitionTo(PlayerState.PlayerReadyState(player))

        assertTrue(player.room == null)
        assertTrue(room.getPlayerList().isEmpty())
    }

}

