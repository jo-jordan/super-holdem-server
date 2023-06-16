package jojo.game.controller

import jojo.game.core.Controller
import jojo.game.dto.ReqData
import jojo.game.dto.RespData
import jojo.game.enums.BetType
import jojo.game.global.GameGlobal
import jojo.game.state.PlayerState
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
        broadcast(param.roomId, param.playerId, JacksonUtils.objectMapper.writeValueAsString(RespData.GameStartDTO()))
    }

    fun dealPlayerCards(param: ReqData.GameDealPlayerCardsDTO) {
        val room = GameGlobal.roomMap.getValue(param.roomId)

        room.getPlayerList().forEach { po ->
            room.getPlayerList().forEach {  pi ->
                if (po.id != pi.id) {
                    po.cardList += listOf(CardUtils.dealNullCard(), CardUtils.dealNullCard())
                } else {
                    po.cardList += listOf(CardUtils.dealCard(), CardUtils.dealCard())
                }
            }
        }

        broadcast(param.roomId, param.playerId, JacksonUtils.objectMapper.writeValueAsString(
            RespData.GameDealPlayerCardsDTO().apply {
                this.playerId = param.playerId
                this.roomId = param.roomId
                this.roomInfo = room.getRoomInfo()
            }
        ), true)
    }

    fun dealFlopCards(param: ReqData.GameDealFlopCardsDTO) {
        // TODO
    }

    fun dealTurnCard(param: ReqData.GameDealTurnCardsDTO) {
        // TODO
    }

    fun dealRiverCard() {
        // TODO
    }

    fun updateBet(param: ReqData.GameUpdateBetDTO) {
        val room = GameGlobal.roomMap.getValue(param.roomId)
        val respData = RespData.GameUpdateBetDTO().apply {
            this.playerId = param.playerId
            this.roomId = param.roomId
            this.roomInfo = room.getRoomInfo()
        }
        broadcast(param.roomId, param.playerId, JacksonUtils.objectMapper.writeValueAsString(respData), true)
    }

    fun bet(param: ReqData.GameBetDTO) {
        val room = GameGlobal.roomMap.getValue(param.roomId)
        val player = room.getPlayerById(param.playerId)
        when (param.betType) {
            BetType.CALL -> {
                // TODO
                player?.transitionTo(PlayerState.PlayerWaitingState(player))
            }
            BetType.RAISE -> {
                // TODO
                player?.transitionTo(PlayerState.PlayerWaitingState(player))
            }
            BetType.FOLD -> {
                player?.isFold = true
                player?.transitionTo(PlayerState.PlayerReadyState(player))
            }
            BetType.CHECK -> {
                // TODO
                player?.transitionTo(PlayerState.PlayerWaitingState(player))
            }
            BetType.ALL_IN -> {
                // TODO
                player?.transitionTo(PlayerState.PlayerWaitingState(player))
            }
            else -> {
                // TODO
            }
        }
    }

    fun calculateWinner() {
        // TODO
    }
}