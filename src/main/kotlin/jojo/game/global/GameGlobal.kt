package jojo.game.global

import jojo.game.entity.Player
import jojo.game.entity.Room

object GameGlobal {
    var roomMap = mutableMapOf<String, Room>()

    var playerMap = mutableMapOf<String, Player>()
}