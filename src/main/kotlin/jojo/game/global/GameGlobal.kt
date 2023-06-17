package jojo.game.global

import jojo.game.entity.Player
import jojo.game.entity.Room
import org.java_websocket.WebSocket

object GameGlobal {
    var roomMap = mutableMapOf<String, Room>()

    var playerMap = mutableMapOf<String, Player>()

    // key: roomId, value: player's connections
    var roomConnections = mutableMapOf<String, MutableMap<String, WebSocket?>>()
}