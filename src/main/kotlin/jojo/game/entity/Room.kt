package jojo.game.entity

import jojo.game.state.RoomState
import jojo.game.utils.CardUtils

class Room {
    var playerList: List<Player> = mutableListOf()
    var id: Int = 0
    var name: String = ""
    var description: String = ""
    var cardList: List<Card> = mutableListOf()

    var machine: RoomState = RoomState.RoomInitState()

    fun transitionTo(state: RoomState) {
        this.machine.exit()
        state.enter()
        this.machine = state
    }

    fun getLeaderboard(): List<Player> {
        return CardUtils.orderPlayerByCard(playerList)
    }
}