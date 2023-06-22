package jojo.game.controller

import jojo.game.core.Controller
import jojo.game.dto.ReqData
import jojo.game.dto.RespData
import jojo.game.enums.BetType
import jojo.game.enums.StageType
import jojo.game.global.GameGlobal
import jojo.game.state.PlayerState
import jojo.game.state.RoomState
import jojo.game.utils.CardUtils
import jojo.game.utils.JacksonUtils
import java.util.*
import kotlin.concurrent.schedule

/**
 * Game controller
 */
class GameController: Controller() {

    fun ready(param: ReqData.GameReadyDTO) {
        val player = GameGlobal.playerMap[param.playerId]
        player?.transitionTo(PlayerState.PlayerReadyState(player))
        player?.room?.updateState()

        broadcast(param.roomId, param.playerId, JacksonUtils.objectMapper.writeValueAsString(
            RespData.GameReadyDTO().apply {
                this.playerId = param.playerId
            }
        ))
    }

    fun start(param: ReqData.GameStartDTO) {
        val room = GameGlobal.roomMap.getValue(param.roomId)

        room.beforeRound()

        room.getPlayerList().forEach { p ->
            sendTo(room.id, p.id, JacksonUtils.objectMapper.writeValueAsString(
                RespData.GameStartDTO().apply {
                    val roomInfo = room.getRoomInfo(p.id)
                    this.roomInfo = roomInfo
                }
            ))
        }

        room.transitionTo(RoomState.RoomDealPlayerCardsState(room))
    }

    fun dealPlayerCards(param: ReqData.GameDealPlayerCardsDTO) {
        val room = GameGlobal.roomMap.getValue(param.roomId)

        room.getPlayerList().forEach { po ->
            val resp = RespData.GameDealPlayerCardsDTO().apply {
                this.playerId = po.id
                this.roomId = param.roomId
            }
            room.getPlayerList().forEach {  pi ->
                if (po.id == pi.id) {
                    pi.cardList += listOf(CardUtils.dealCard(), CardUtils.dealCard())
                    resp.cards = pi.cardList
                } else {
                    resp.othersCards[pi.id] = listOf(CardUtils.dealNullCard(), CardUtils.dealNullCard())
                }
            }

            sendTo(param.roomId, po.id, JacksonUtils.objectMapper.writeValueAsString(resp))
        }

        room.stage = StageType.BET_AFTER_DEAL_PLAYER_CARDS
        room.transitionTo(RoomState.RoomBettingState(room))
    }

    fun dealFlopCards(param: ReqData.GameDealFlopCardsDTO) {
        val room = GameGlobal.roomMap.getValue(param.roomId)
        room.cards += listOf(CardUtils.dealCard(), CardUtils.dealCard(), CardUtils.dealCard())

        broadcast(param.roomId, param.playerId, JacksonUtils.objectMapper.writeValueAsString(
            RespData.GameDealFlopCardsDTO().apply {
                this.playerId = param.playerId
                this.roomId = param.roomId
                this.cards = room.cards
            }
        ), true)

        room.stage = StageType.BET_AFTER_DEAL_FLOP_CARDS
        room.transitionTo(RoomState.RoomBettingState(room))
    }

    fun dealTurnCard(param: ReqData.GameDealTurnCardsDTO) {
        val room = GameGlobal.roomMap.getValue(param.roomId)
        val card = listOf(CardUtils.dealCard())
        room.cards += card

        broadcast(param.roomId, param.playerId, JacksonUtils.objectMapper.writeValueAsString(
            RespData.GameDealTurnCardsDTO().apply {
                this.playerId = param.playerId
                this.roomId = param.roomId
                this.cards = card
            }
        ), true)

        room.stage = StageType.BET_AFTER_DEAL_TURN_CARDS
        room.transitionTo(RoomState.RoomBettingState(room))
    }

    fun dealRiverCard(param: ReqData.GameDealRiverCardsDTO) {
        val room = GameGlobal.roomMap.getValue(param.roomId)
        val card = listOf(CardUtils.dealCard())
        room.cards += card

        broadcast(param.roomId, param.playerId, JacksonUtils.objectMapper.writeValueAsString(
            RespData.GameDealRiverCardsDTO().apply {
                this.playerId = param.playerId
                this.roomId = param.roomId
                this.cards = card
            }
        ), true)

        room.stage = StageType.BET_AFTER_DEAL_RIVER_CARDS
        room.transitionTo(RoomState.RoomBettingState(room))
    }

    /**
     * Update bet for each player
     */
    fun updateBet(param: ReqData.GameUpdateBetDTO) {
        val room = GameGlobal.roomMap.getValue(param.roomId)

        room.getPlayerList().forEach {
            val respData = RespData.GameUpdateBetDTO().apply {
                this.playerId = param.playerId
                this.roomId = param.roomId
            }

            if (it.isFold || it.state is PlayerState.PlayerWaitingState) {
                respData.operationList[it.id] = listOf()
            } else {
                respData.operationList[it.id] = room.getBetTypes()
            }
            respData.roomInfo = room.getRoomInfo(it.id)

            sendTo(param.roomId, it.id, JacksonUtils.objectMapper.writeValueAsString(respData))
        }
    }

    fun bet(param: ReqData.GameBetDTO) {
        val room = GameGlobal.roomMap.getValue(param.roomId)
        val player = room.getPlayerById(param.playerId)

        if (player?.state !is PlayerState.PlayerBettingState) {
            return
        }

        var actualBetAmount = 0
        when (param.betType) {
            BetType.BET -> {
                // TODO
                actualBetAmount = room.recordBet(param.betAmount)
                player.transitionTo(PlayerState.PlayerWaitingState(player))
            }
            BetType.CALL -> {
                // TODO
                actualBetAmount = room.recordCall()
                player.transitionTo(PlayerState.PlayerWaitingState(player))
            }
            BetType.RAISE -> {
                // TODO
                actualBetAmount = room.recordRaise(param.betAmount)
                player.transitionTo(PlayerState.PlayerWaitingState(player))
            }
            BetType.FOLD -> {
                room.recordFold()
                player.transitionTo(PlayerState.PlayerReadyState(player))
            }
            BetType.CHECK -> {
                // TODO
                room.recordCheck()
                player.transitionTo(PlayerState.PlayerWaitingState(player))
            }
            BetType.ALL_IN -> {
                // TODO
                actualBetAmount = room.recordAllIn()
                player.transitionTo(PlayerState.PlayerWaitingState(player))
            }
            else -> {
                // TODO
            }
        }

        broadcast(param.roomId, param.playerId, JacksonUtils.objectMapper.writeValueAsString(
            RespData.GameBetInfoDTO().apply {
                this.playerId = param.playerId
                this.roomId = param.roomId
                this.playerName = player.name ?: ""
                this.betType = param.betType
                this.betAmount = actualBetAmount
                this.roomBetPool = room.getPlayerList().sumOf { it.getBetLog().sumOf { bet -> bet.betAmount } }
            }
        ), true)

        room.lastBetPlayer = room.currentBetPlayer
        room.currentBetPlayer = room.findNextActivePlayer(room.currentBetPlayer!!)
        room.updateState()
    }

    fun result(param: ReqData.GameResultDTO) {
        val room = GameGlobal.roomMap.getValue(param.roomId)
        room.calculateWinner()
        val leaderboard = room.getLeaderboard()

        val playerInfoList = leaderboard.map { p ->
            RespData.PlayerInfoDTO().apply {
                this.betAmount = p.getBetLog().sumOf { it.betAmount }
                this.username = p.name
                this.roomId = param.roomId
                this.playerId = p.id
                this.cardList = p.cardList
                this.winChips = p.getBetLog().sumOf { it.betAmount }
                this.position = p.position
            }
        }

        val winner = playerInfoList.first()
        winner.winChips = room.getWinnerChips()

        val respData = RespData.GameResultDTO(winner = winner).apply {
            this.playerId = param.playerId
            this.roomId = param.roomId
            this.leaderboards = playerInfoList
        }

        broadcast(param.roomId, param.playerId, JacksonUtils.objectMapper.writeValueAsString(respData), true)

        if (room.round < room.gameConfig.maxRound) {
            Timer("schedule", true).schedule(15000) {
                room.transitionTo(RoomState.RoomStartState(room))
            }
        }
    }
}