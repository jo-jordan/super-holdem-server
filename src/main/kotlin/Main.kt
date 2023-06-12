import jojo.game.core.Dispatcher
import jojo.game.entity.Player
import jojo.game.entity.Room
import jojo.game.state.PlayerState
import jojo.game.state.RoomState
import jojo.game.utils.CardUtils
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress


class Server(port: Int): WebSocketServer(InetSocketAddress(port)) {
    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        println("New connection from " + conn?.remoteSocketAddress?.address?.hostAddress)
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        println("Closed connection to " + conn?.remoteSocketAddress?.address?.hostAddress)
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        Dispatcher.dispatch(message, conn)
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        println("Error: " + ex?.message)
    }

    override fun onStart() {
        println("Server started!")
    }
}

fun main(args: Array<String>) {

    var logger = LoggerFactory.getLogger("Main")
    println("Hello World!")

//    var port = 8888 // 843 flash policy port
//
//    try {
//        port = args[0].toInt()
//    } catch (ex: Exception) {
//    }
//    val s = Server(port)
//    s.start()
//    System.out.println("ChatServer started on port: " + s.getPort())
    CardUtils.init()
    CardUtils.shuffle()
    val room = Room()
    room.transitionTo(RoomState.RoomInitState())

    for (i in 1..8) {
        val player = Player().apply {
            id = i
            this.room = room
        }

        room.playerList += (player)

        for (j in 1..2) {
            val card1 = CardUtils.dealCard()

            player.cardList += (card1)

            logger.info("Player id: ${player.id} : ${card1.color.value} ${card1.value}")
        }
    }

    for (i in 1..5) {
        val card = CardUtils.dealCard()
        room.cardList += (card)

        logger.info("Table: ${card.color.value} ${card.value}")
    }

    room.playerList.forEach {
        val pair = it.calculateMaxCardType()

        logger.info("Player id: ${it.id} : ${pair.first} ${pair.second.map { it.color.value + it.value }}")
    }

    val list = room.getLeaderboard()
    logger.info("Leaderboard: ${list.map { it.id }}")


}