package jojo.game.state

import jojo.game.core.Dispatcher
import jojo.game.dto.ReqData
import jojo.game.entity.Room
import jojo.game.enums.BetType
import jojo.game.enums.Position
import jojo.game.enums.StageType
import jojo.game.utils.CardUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

sealed class RoomState(open val room: Room) {

    val logger: Logger = LoggerFactory.getLogger("RoomState")

    protected open var period: Long = 5000L

    open fun enter() {}

    open fun exit() {}

    open fun update() {}

    class RoomInitState(override val room: Room) : RoomState(room) {
        override fun enter() {
            logger.trace("Room is initializing.")
        }

        override fun exit() {
            logger.trace("Room has initialized.")
            room.transitionTo(RoomReadyState(room))
        }
    }

    class RoomReadyState(override val room: Room) : RoomState(room) {
        override fun enter() {
            logger.trace("Room is ready.")
        }

        override fun update() {
            if (room.getPlayerList().size == room.gameConfig.numToStart) {
                room.transitionTo(RoomStartState(room))
            }
        }

        override fun exit() {
            logger.trace("Room has readied.")
        }
    }

    class RoomStartState(override val room: Room) : RoomState(room) {
        override fun enter() {
            CardUtils.init()

            val reqData = ReqData.GameStartDTO().apply {
                this.roomId = room.id
            }
            Dispatcher.dispatch(reqData)

            logger.trace("Room is starting.")
        }

        override fun exit() {
            logger.trace("Room has started.")
        }
    }

    class RoomDealPlayerCardsState(override val room: Room) : RoomState(room) {
        override fun enter() {
            val reqData = ReqData.GameDealPlayerCardsDTO().apply {
                this.roomId = room.id
            }
            Dispatcher.dispatch(reqData)

            logger.trace("Room is dealing player cards.")
        }

        override fun exit() {
            logger.trace("Room has dealt player cards.")
        }
    }

    class RoomBettingState(override val room: Room) : RoomState(room) {

        override fun enter() {
            logger.trace("Room is betting.")
            room.getPlayerList().forEach {
                it.transitionTo(PlayerState.PlayerWaitingState(it))
            }
            if (checkBetsEqual()) {
                transitionToNextState()
            } else {
                continueCurrentRound()
            }
        }

        private fun transitionToNextState() {
            when (room.stage) {
                StageType.BET_AFTER_DEAL_PLAYER_CARDS -> {
                    room.transitionTo(RoomDealFlopCardsState(room))
                }
                StageType.BET_AFTER_DEAL_FLOP_CARDS -> {
                    room.transitionTo(RoomDealTurnCardState(room))
                }
                StageType.BET_AFTER_DEAL_TURN_CARDS -> {
                    room.transitionTo(RoomDealRiverCardState(room))
                }
                StageType.BET_AFTER_DEAL_RIVER_CARDS -> {
                    room.transitionTo(RoomEndState(room))
                }

                else -> {}
            }
        }

        private fun isAllPlayerAllIn(): Boolean {
            return room.getPlayerList().filter { !it.isFold }.all { it.getChipsAmount() == 0 }
        }

        override fun update() {
            if (checkBetsEqual()) {
                transitionToNextState()
            } else {
                continueCurrentRound()
            }
        }

        private fun continueCurrentRound() {
            // update currentBetPlayer to the next active player
            val lastBetType = room.lastBetPlayer?.getBetLog()?.last()?.betType
            val lastBetChips = room.lastBetPlayer?.getBetLog()?.sumOf { it.betAmount } ?: 0
            val currentBetChips = room.currentBetPlayer?.getBetLog()?.sumOf{ it.betAmount } ?: 0
            when (lastBetType) {
                BetType.CALL -> {
                    if (room.currentBetPlayer?.position == Position.BIG_BLIND
                        && room.betRound == 0
                        && lastBetChips == currentBetChips) {
                        room.updateBetTypes(listOf(BetType.CHECK, BetType.FOLD, BetType.RAISE, BetType.ALL_IN))
                    } else {
                        room.updateBetTypes(listOf(BetType.CALL, BetType.FOLD, BetType.RAISE, BetType.ALL_IN))
                    }
                }
                BetType.RAISE -> {
                    room.updateBetTypes(listOf(BetType.CALL, BetType.FOLD, BetType.RAISE, BetType.ALL_IN))
                }
                BetType.ALL_IN -> {
                    room.updateBetTypes(listOf(BetType.CALL, BetType.FOLD, BetType.RAISE))
                }
                else -> {
                    if (room.betRound == 0) {
                        room.updateBetTypes(listOf(BetType.CALL, BetType.FOLD, BetType.RAISE, BetType.ALL_IN))
                    } else {
                        room.updateBetTypes(listOf(BetType.CALL, BetType.CHECK, BetType.FOLD, BetType.RAISE, BetType.ALL_IN))
                    }
                }
            }

            // update player state to betting
            room.currentBetPlayer?.transitionTo(PlayerState.PlayerBettingState(room.currentBetPlayer!!))
        }

        private fun checkBetsEqual(): Boolean {
            if (isAllPlayerAllIn()) {
                room.updateBetTypes(listOf())
                val reqData = ReqData.GameUpdateBetDTO().apply {
                    this.roomId = room.id
                    this.playerId = room.currentBetPlayer?.id ?: ""
                }
                Dispatcher.dispatch(reqData)
                return true
            }

            val activePlayers = room.getPlayerList().filter { !it.isFold }
            val maxBet = activePlayers.maxOfOrNull { it.getBetLog().sumOf { log -> log.betAmount } }

            // 额外的条件，用于检查当前的下注玩家是否为大盲位玩家
            if (room.betRound == 0 && room.lastBetPlayer?.position != Position.BIG_BLIND) {
                return false
            }

            return activePlayers.all { player ->
                player.getBetLog().lastOrNull()?.betRound == room.betRound && player.getBetLog().sumOf { it.betAmount } == maxBet
            }
        }

        override fun exit() {

            room.betRound++
            if (room.betRound > 0) {  // after the first round
                room.currentBetPlayer = room.getPlayerList().find { it.position == Position.SMALL_BLIND }
            }
            logger.trace("Room has bet.")
        }
    }

    class RoomDealFlopCardsState(override val room: Room) : RoomState(room) {
        override fun enter() {
            logger.trace("Room is dealing flop cards.")
            val reqData = ReqData.GameDealFlopCardsDTO().apply {
                this.roomId = room.id
            }
            Dispatcher.dispatch(reqData)
        }

        override fun exit() {
            logger.trace("Room has dealt flop cards.")
        }
    }

    class RoomDealTurnCardState(override val room: Room) : RoomState(room) {
        override fun enter() {
            logger.trace("Room is dealing turn card.")
            val reqData = ReqData.GameDealTurnCardsDTO().apply {
                this.roomId = room.id
            }
            Dispatcher.dispatch(reqData)
        }

        override fun exit() {
            logger.trace("Room has dealt turn card.")
        }
    }

    class RoomDealRiverCardState(override val room: Room) : RoomState(room) {
        override fun enter() {
            logger.trace("Room is dealing river card.")
            val reqData = ReqData.GameDealRiverCardsDTO().apply {
                this.roomId = room.id
            }
            Dispatcher.dispatch(reqData)
        }

        override fun exit() {
            logger.trace("Room has dealt river card.")
        }
    }

    class RoomEndState(override val room: Room) : RoomState(room) {
        override fun enter() {
            val reqData = ReqData.GameResultDTO().apply {
                this.roomId = room.id
            }
            Dispatcher.dispatch(reqData)

            logger.trace("Room is ending.")
        }

        override fun exit() {
            logger.trace("Room has ended.")
        }
    }
}