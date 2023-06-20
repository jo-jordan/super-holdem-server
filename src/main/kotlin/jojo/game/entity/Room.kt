package jojo.game.entity

import jojo.game.core.rotate
import jojo.game.dto.RespData
import jojo.game.enums.BetType
import jojo.game.enums.Position
import jojo.game.enums.StageType
import jojo.game.global.GameGlobal
import jojo.game.state.RoomState
import jojo.game.utils.CardUtils
import org.java_websocket.WebSocket
import java.util.*
import kotlin.math.absoluteValue

class Room(val id: String = UUID.randomUUID().toString()) {
    private var playerList: List<Player> = mutableListOf()
    var name: String = ""
    var cards: List<Card> = mutableListOf()
    var lastStateName = ""
    var gameConfig: GameConfig = GameConfig()
    // record round count in the game, after the winner is determined, round will be increased by 1
    var round: Int  = 0

    var betRound: Int = 0
    var lastBetPlayer: Player? = null
    var nextToBetPlayer: Player? = null
    var totalScore: Int = 0
    var stage: StageType = StageType.BET_AFTER_DEAL_PLAYER_CARDS

    private var betTypes: List<BetType> = mutableListOf()

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
        this.lastStateName = state.javaClass.simpleName

        this.state = state
        state.enter()
    }

    fun updateBetTypes(types: List<BetType>) {
        this.betTypes = types
    }

    fun updateWinScore() {

    }

    fun calculateWinner() {
        playerList.forEach {
            it.calculateMaxCardType()
        }
    }

    fun getLeaderboard(): List<Player> {
        val list = CardUtils.orderPlayerByCard(playerList)
        list.forEach { it.score = it.maxCardType.second.maxOf { v -> v.value } }
        return list
    }

    fun addPlayer(player: Player) {
        player.room = this
        playerList = playerList + player
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
        this.totalScore = 0
        this.cards = mutableListOf()
        this.lastStateName = ""
        this.betRound = 0
        this.nextToBetPlayer = null
    }

    fun updateRound() {
        reset()
        updatePlayerPosition()
        blindFirstBet()
        round++
    }

    private fun blindFirstBet() {
        playerList.find { it.position == Position.SMALL_BLIND }?.let {
            it.updateChips(-gameConfig.smallBlind)
            it.addBetLog(BetLog(this.betRound, BetType.NONE, -gameConfig.smallBlind))
            lastBetPlayer = it
            nextToBetPlayer = it.nextPlayer
        }

        playerList.find { it.position == Position.BIG_BLIND }?.let {
            it.updateChips(-gameConfig.bigBlind)
            it.addBetLog(BetLog(this.betRound, BetType.NONE, -gameConfig.bigBlind))
            lastBetPlayer = it
            nextToBetPlayer = it.nextPlayer
        }
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
            player.position = newPositionList[index]
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

    fun getRoomInfo(playerId: String): RespData.RoomInfoDTO {
        var playerInfoList = getPlayerInfoList()
        playerInfoList = playerInfoList.rotate{ pi -> pi.playerId == playerId }
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
                this.betAmount = it.getBetLog().sumOf { it.betAmount }
                this.cardList = it.cardList
                this.position = it.position
                this.roomId = it.room?.id ?: ""
                this.chips = it.getChipsAmount()
            }
        }
    }

    fun getBetTypes(): List<BetType> {
        return betTypes
    }

    fun recordCall() {
        val callScore = (lastBetPlayer?.getBetLog()?.lastOrNull()?.betAmount?.absoluteValue ?: 0) -
                (nextToBetPlayer?.getBetLog()?.lastOrNull()?.betAmount?.absoluteValue ?: 0)

        nextToBetPlayer?.updateChips(-callScore)
        nextToBetPlayer?.addBetLog(BetLog(this.betRound, BetType.CALL, -callScore))
    }

    fun recordRaise(betAmount: Int) {
        nextToBetPlayer?.updateChips(-betAmount)
        nextToBetPlayer?.addBetLog(BetLog(this.betRound, BetType.RAISE, -betAmount))
    }

    fun recordFold() {
        nextToBetPlayer?.addBetLog(BetLog(this.betRound, BetType.FOLD, 0))
        nextToBetPlayer?.isFold = true
    }

    fun recordCheck() {
        nextToBetPlayer?.addBetLog(BetLog(this.betRound, BetType.CHECK, 0))
    }

    fun recordAllIn() {
        val remaining = nextToBetPlayer?.getChipsAmount() ?: 0
        nextToBetPlayer?.updateChips(-remaining)
        nextToBetPlayer?.addBetLog(BetLog(this.betRound, BetType.ALL_IN, -remaining))
    }

}