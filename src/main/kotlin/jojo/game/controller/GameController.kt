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

        broadcast(param.roomId, param.playerId, JacksonUtils.objectMapper.writeValueAsString(RespData.GameStartDTO()))

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
        room.cards += listOf(CardUtils.dealCard())

        broadcast(param.roomId, param.playerId, JacksonUtils.objectMapper.writeValueAsString(
            RespData.GameDealTurnCardsDTO().apply {
                this.playerId = param.playerId
                this.roomId = param.roomId
                this.cards = room.cards
            }
        ), true)

        room.stage = StageType.BET_AFTER_DEAL_TURN_CARDS
        room.transitionTo(RoomState.RoomBettingState(room))
    }

    fun dealRiverCard(param: ReqData.GameDealRiverCardsDTO) {
        val room = GameGlobal.roomMap.getValue(param.roomId)
        room.cards += listOf(CardUtils.dealCard())

        broadcast(param.roomId, param.playerId, JacksonUtils.objectMapper.writeValueAsString(
            RespData.GameDealRiverCardsDTO().apply {
                this.playerId = param.playerId
                this.roomId = param.roomId
                this.cards = room.cards
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

        val respData = RespData.GameUpdateBetDTO().apply {
            this.playerId = param.playerId
            this.roomId = param.roomId
        }

        room.getPlayerList().forEach {
            if (it.isFold || it.state is PlayerState.PlayerWaitingState) {
                respData.operationList[it.id] = listOf()
            } else {
                respData.operationList[it.id] = room.getBetTypes()
            }
        }

        broadcast(param.roomId, param.playerId, JacksonUtils.objectMapper.writeValueAsString(respData), true)
    }

    fun bet(param: ReqData.GameBetDTO) {
        val room = GameGlobal.roomMap.getValue(param.roomId)
        val player = room.getPlayerById(param.playerId)
        when (param.betType) {
            BetType.CALL -> {
                // TODO
                room.recordCall()
                player?.transitionTo(PlayerState.PlayerWaitingState(player))
            }
            BetType.RAISE -> {
                // TODO
                room.recordRaise(param.betAmount)
                player?.transitionTo(PlayerState.PlayerWaitingState(player))
            }
            BetType.FOLD -> {
                room.recordFold()
                player?.transitionTo(PlayerState.PlayerReadyState(player))
            }
            BetType.CHECK -> {
                // TODO
                room.recordCheck()
                player?.transitionTo(PlayerState.PlayerWaitingState(player))
            }
            BetType.ALL_IN -> {
                // TODO
                room.recordAllIn()
                player?.transitionTo(PlayerState.PlayerWaitingState(player))
            }
            else -> {
                // TODO
            }
        }
        room.updateState()
    }

    fun result(param: ReqData.GameResultDTO) {
        val room = GameGlobal.roomMap.getValue(param.roomId)
        room.calculateWinner()
        val leaderboard =  room.getLeaderboard()

        val playerInfoList = leaderboard.map {p ->
            RespData.PlayerInfoDTO().apply {
                this.playerId = p.id
                this.cardList = p.cardList
                this.winChips = p.getChipsAmount() + p.getBetLog().sumOf { it.betAmount }
            }
        }
        val respData = RespData.GameResultDTO(winner = playerInfoList.first()).apply {
            this.playerId = param.playerId
            this.roomId = param.roomId
            this.leaderboards = playerInfoList
        }

        broadcast(param.roomId, param.playerId, JacksonUtils.objectMapper.writeValueAsString(respData), true)
    }
}