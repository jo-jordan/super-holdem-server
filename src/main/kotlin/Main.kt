import com.sun.net.httpserver.HttpServer
import jojo.game.AuthServer
import jojo.game.GameServer
import jojo.game.entity.Player
import jojo.game.entity.Room
import jojo.game.state.RoomState
import jojo.game.utils.CardUtils
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

fun main(args: Array<String>) {
    val authPort = 8887
    val gamePort = 8888

    val authServer = AuthServer(authPort)
    val gameServer = GameServer(gamePort)

    authServer.start()
    gameServer.start()


}