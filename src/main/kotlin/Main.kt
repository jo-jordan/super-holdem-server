import jojo.game.AuthServer
import jojo.game.GameServer

fun main(args: Array<String>) {
    val authPort = 8887
    val gamePort = 8888

    val authServer = AuthServer(authPort)
    val gameServer = GameServer(gamePort)

    authServer.start()
    gameServer.start()
}