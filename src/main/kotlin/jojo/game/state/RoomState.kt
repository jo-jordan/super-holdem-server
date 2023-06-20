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
            room.getPlayerList().forEach {
                it.transitionTo(PlayerState.PlayerWaitingState(it))
            }
            logger.trace("Room is betting.")
            continueCurrentRound()
        }

        override fun update() {
            if (checkBetsEqual()) {
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
            } else {
                continueCurrentRound()
            }
        }

        private fun continueCurrentRound() {
            if (room.nextToBetPlayer == null) {
                room.nextToBetPlayer = room.getPlayerList().first()
                room.updateBetTypes(listOf(BetType.FOLD, BetType.RAISE, BetType.ALL_IN))
            } else {
                if (room.lastBetPlayer?.getBetLog()?.last()?.betType == BetType.CHECK) {
                    room.updateBetTypes(listOf(BetType.CALL, BetType.CHECK, BetType.FOLD, BetType.RAISE, BetType.ALL_IN))
                } else {
                    room.updateBetTypes(listOf(BetType.CALL, BetType.FOLD, BetType.RAISE, BetType.ALL_IN))
                }

                if (room.nextToBetPlayer?.position == Position.BIG_BLIND && room.betRound == 0) {
                    room.betRound++
                    room.updateBetTypes(listOf(BetType.CHECK, BetType.FOLD, BetType.RAISE, BetType.ALL_IN))
                }

                if (room.betRound >= 2) {
                    room.getPlayerList().first { (it.position == Position.SMALL_BLIND && !it.isFold) || !it.isFold }.let {
                        room.lastBetPlayer = room.nextToBetPlayer
                        room.nextToBetPlayer = it
                    }
                }
            }
            room.nextToBetPlayer?.transitionTo(PlayerState.PlayerBettingState(room.nextToBetPlayer!!))
        }

        private fun checkBetsEqual(): Boolean {
            if (room.nextToBetPlayer?.position == Position.BIG_BLIND && room.betRound == 0) return false
            val maxBet = room.getPlayerList().maxOfOrNull { it.getBetLog().sumOf { log -> log.betAmount } }
            return room.getPlayerList().all { player -> player.getBetLog().sumOf { it.betAmount } == maxBet }
        }

        override fun exit() {

            room.betRound++

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