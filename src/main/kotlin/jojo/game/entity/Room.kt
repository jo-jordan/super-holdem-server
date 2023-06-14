package jojo.game.entity

import jojo.game.state.RoomState
import jojo.game.utils.CardUtils
import java.util.UUID

class Room(val id: String = UUID.randomUUID().toString()) {
    private var playerList: List<Player> = mutableListOf()
    var name: String = ""
    var cardList: List<Card> = mutableListOf()

    private var state: RoomState? = null

    fun currentState(): RoomState? {
        return state
    }

    fun transitionTo(state: RoomState) {
        if (this.state == null) {
            state.enter()
            this.state = state
            return
        }
        this.state?.exit()
        state.enter()
        this.state = state
    }

    fun getLeaderboard(): List<Player> {
        val list = CardUtils.orderPlayerByCard(playerList)
        list.forEach { it.score = it.maxCardType.second.maxOf { v -> v.value } }
        return list
    }

    fun addPlayer(player: Player) {
        playerList = playerList + player
        player.room = this
    }

    fun removePlayer(player: Player) {
        playerList = playerList - player
    }

    fun removePlayerById(id: String) {
        playerList = playerList.filter { it.id != id }
    }

    fun getPlayerById(id: String): Player? {
        return playerList.find { it.id == id }
    }

    fun getPlayerList(): List<Player> {
        return playerList
    }
}