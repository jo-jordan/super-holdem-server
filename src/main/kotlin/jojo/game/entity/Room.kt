package jojo.game.entity

import jojo.game.dto.RespData
import jojo.game.enums.BetType
import jojo.game.enums.Position
import jojo.game.global.GameGlobal
import jojo.game.state.RoomState
import jojo.game.utils.CardUtils
import org.java_websocket.WebSocket
import java.util.UUID

class Room(val id: String = UUID.randomUUID().toString()) {
    private var playerList: List<Player> = mutableListOf()
    var name: String = ""
    var cardList: List<Card> = mutableListOf()
    var lastStateName = ""
    var gameConfig: GameConfig = GameConfig()
    var round: Int  = 0
    var nextToBetPlayer: Player? = null

    private var state: RoomState? = null

    fun currentState(): RoomState? {
        return state
    }

    fun updateState() {
        this.state?.update()
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
        this.lastStateName = state.javaClass.simpleName
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

    fun addPlayerConnection(playerId: String, conn: WebSocket?) {
        if (!GameGlobal.roomConnections.containsKey(id))
            GameGlobal.roomConnections[id] = mutableMapOf()

        val map = GameGlobal.roomConnections.getValue(id)
        map[playerId] = conn
    }

    fun removePlayerConnection(playerId: String) {
        if (!GameGlobal.roomConnections.containsKey(id))
            GameGlobal.roomConnections[id] = mutableMapOf()

        val map = GameGlobal.roomConnections.getValue(id)
        map.remove(playerId)
    }

    private fun reset() {
        // TODO
    }

    fun updateRound() {
        reset()
        updatePlayerPosition()
        blindFirstBet()
        round++
    }

    private fun blindFirstBet() {
        playerList.find { it.seatType == Position.SMALL_BLIND }?.betMap?.put(BetType.NONE.name, gameConfig.smallBlind)
        playerList.find { it.seatType == Position.BIG_BLIND }?.betMap?.put(BetType.NONE.name, gameConfig.bigBlind)
    }

    private fun updatePlayerPosition() {
        val playerCount = playerList.size
        if (playerCount < 3 || playerCount > 9) return

        val positions = when(playerCount) {
            3 -> listOf(Position.BUTTON, Position.SMALL_BLIND, Position.BIG_BLIND)
            4 -> listOf(Position.BUTTON, Position.SMALL_BLIND, Position.BIG_BLIND, Position.UNDER_THE_GUN)
            5 -> listOf(Position.BUTTON, Position.SMALL_BLIND, Position.BIG_BLIND, Position.UNDER_THE_GUN, Position.HIJACK)
            6 -> listOf(Position.BUTTON, Position.SMALL_BLIND, Position.BIG_BLIND, Position.UNDER_THE_GUN, Position.HIJACK, Position.CUT_OFF)
            7 -> listOf(Position.BUTTON, Position.SMALL_BLIND, Position.BIG_BLIND, Position.UNDER_THE_GUN, Position.UNDER_THE_GUN_PLUS_ONE, Position.HIJACK, Position.CUT_OFF)
            8 -> listOf(Position.BUTTON, Position.SMALL_BLIND, Position.BIG_BLIND, Position.UNDER_THE_GUN, Position.UNDER_THE_GUN_PLUS_ONE, Position.MID_POSITION, Position.HIJACK, Position.CUT_OFF)
            9 -> listOf(Position.BUTTON, Position.SMALL_BLIND, Position.BIG_BLIND, Position.UNDER_THE_GUN, Position.UNDER_THE_GUN_PLUS_ONE, Position.MID_POSITION, Position.MID_POSITION_PLUS_ONE, Position.HIJACK, Position.CUT_OFF)
            else -> listOf()
        }

        val newPositionList = mutableListOf<Position>()
        newPositionList.addAll(positions.subList(round % playerCount, positions.size))
        newPositionList.addAll(positions.subList(0, round % playerCount))

        playerList.forEachIndexed { index, player ->
            player.seatType = newPositionList[index]
            // 根据玩家列表的顺序设置每个玩家的nextPlayer属性
            if (!player.isFold) {
                var nextIndex = index + 1
                // 寻找下一个没有弃牌的玩家
                while (nextIndex < playerList.size && playerList[nextIndex].isFold) {
                    nextIndex++
                }
                player.nextPlayer = if (nextIndex < playerList.size) {
                    playerList[nextIndex]
                } else {
                    // 当当前玩家是列表中的最后一个时，查找列表开始的第一个没有弃牌的玩家
                    playerList.firstOrNull { !it.isFold }
                }
            }
        }
    }

    fun getRoomInfo(): RespData.RoomInfoDTO {
        val playerInfoList = getPlayerInfoList()
        val roomBet = playerInfoList.sumOf { it.betAmount }
        return RespData.RoomInfoDTO().apply {
            this.roomId = id
            this.roomName = name
            this.betAmount = roomBet
            this.playerInfoList = playerInfoList
        }
    }

    private fun getPlayerInfoList(): List<RespData.PlayerInfoDTO> {
        return playerList.map {
            RespData.PlayerInfoDTO().apply {
                this.playerId = it.id
                this.username = it.name
                this.betAmount = it.betMap.values.sum()
                this.cardList = it.cardList
            }
        }
    }

}