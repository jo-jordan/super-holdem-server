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
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.CountDownLatch

class MainKtTest {

    private val logger = LoggerFactory.getLogger("MainKtTest")

    private var loginResult: RespData.LoginDTO? = null

    private var roomId = ""

    private var gameClient: WebSocketClient? = null

    companion object {
        private val authServer = AuthServer(8887)
        private val gameServer = GameServer(8888)
        @JvmStatic
        @BeforeAll
        @Order(1)
        fun start_server() {
            authServer.start()
            gameServer.start()
        }

        @JvmStatic
        @AfterAll
        fun stop_server() {
            authServer.stop()
            gameServer.stop()
        }
    }

    @BeforeEach
    fun login_should_work() {
        if (loginResult != null) return
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
        assertTrue(loginResult?.username == "test")
    }

    private fun sendAndGetResult(message: String): RespData? {
        val countDownLatch = CountDownLatch(1)
        var result: RespData? = null

        gameClient = object : WebSocketClient(
            URI.create("ws://127.0.0.1:8888"),
            mapOf(Pair(CommonHeaders.HEADER_PLAYER_ID, loginResult?.playerId), Pair(CommonHeaders.HEADER_ROOM_ID, roomId))) {

            override fun onOpen(handshakedata: ServerHandshake?) {
                logger.info("Connect to game server success")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                logger.info("Game server onClose")
            }

            override fun onMessage(message: String?) {
                logger.info("Receive message from game server: $message")
                result = JacksonUtils.objectMapper.readValue(message, RespData::class.java)
                countDownLatch.countDown()
            }

            override fun onError(ex: Exception?) {
                logger.error("Game server onError")
            }
        }
        gameClient?.connectBlocking()

        gameClient?.send(message)
        countDownLatch.await()

        return result
    }


    @Test
    fun player_create_room_should_work() {
        var result = sendAndGetResult(JacksonUtils.objectMapper.writeValueAsString(ReqData.RoomCreateDTO().apply { this.name = "TestRoom" }))
        assertTrue(result is RespData.RoomCreateDTO)
        this.roomId = (result as RespData.RoomCreateDTO).roomId

        result = sendAndGetResult(JacksonUtils.objectMapper.writeValueAsString(ReqData.RoomSearchDTO().apply { this.keyword = "TestRoom" }))
        val roomSearchResult = result as RespData.RoomSearchDTO
        assertTrue(roomSearchResult.roomList.isNotEmpty())
        assertTrue(roomSearchResult.roomList[0].roomName == "TestRoom")
    }

    @Test
    fun player_leave_should_work() {
        var result = sendAndGetResult(JacksonUtils.objectMapper.writeValueAsString(ReqData.RoomCreateDTO().apply { this.name = "TestRoom" }))
        assertTrue(result is RespData.RoomCreateDTO)
        this.roomId = (result as RespData.RoomCreateDTO).roomId

        result = sendAndGetResult(JacksonUtils.objectMapper.writeValueAsString(ReqData.RoomSearchDTO().apply { this.keyword = "TestRoom" }))
        val roomSearchResult = result as RespData.RoomSearchDTO
        assertTrue(roomSearchResult.roomList.isNotEmpty())
        assertTrue(roomSearchResult.roomList[0].roomName == "TestRoom")

        result = sendAndGetResult(JacksonUtils.objectMapper.writeValueAsString(ReqData.RoomLeaveDTO()))
        assertTrue(result is RespData.RoomLeaveDTO)
    }

    @Test
    fun player_ready_should_work() {
        var result = sendAndGetResult(JacksonUtils.objectMapper.writeValueAsString(ReqData.GameReadyDTO()))
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

