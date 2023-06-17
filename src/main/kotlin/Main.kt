import jojo.game.AuthServer
import jojo.game.GameServer
import jojo.game.entity.Player
import jojo.game.entity.Room
import jojo.game.state.RoomState
import jojo.game.utils.CardUtils
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {

    val logger = LoggerFactory.getLogger("Main")

    val authPort = 8887
    val gamePort = 8888

    val authServer = AuthServer(authPort)
    val gameServer = GameServer(gamePort)

    authServer.start()
    gameServer.start()

    CardUtils.init()
    CardUtils.shuffle()
    val room = Room()
    room.transitionTo(RoomState.RoomInitState(room))

    for (i in 1..8) {
        val player = Player().apply {
            id = "$i"
            this.room = room
        }

        room.addPlayer(player)

        for (j in 1..2) {
            val card1 = CardUtils.dealCard()

            player.cardList += (card1)

            logger.info("Player playerId: ${player.id} : ${card1.color.value} ${card1.value}")
        }
    }

    for (i in 1..5) {
        val card = CardUtils.dealCard()
        room.cards += (card)

        logger.info("Table: ${card.color.value} ${card.value}")
    }

    room.getPlayerList().forEach {
        val pair = it.calculateMaxCardType()

        logger.info("Player playerId: ${it.id} : ${pair.first} ${pair.second.map { it.color.value + it.value }}")
    }

    val list = room.getLeaderboard()
    logger.info("Leaderboard: ${list.map { it.id }}")


}