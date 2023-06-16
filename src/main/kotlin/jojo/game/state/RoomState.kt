package jojo.game.state

import jojo.game.core.Dispatcher
import jojo.game.dto.ReqData
import jojo.game.entity.Room
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.timer

sealed class RoomState(open val room: Room) {

    val logger: Logger = LoggerFactory.getLogger("RoomState")

    protected open var period: Long = 5000L

    open fun enter() {
        timer("PlayerStateTimer", false, 0, period) {
            exit()
        }
    }
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
                exit()
            }
        }

        override fun exit() {
            room.transitionTo(RoomStartState(room))
            logger.trace("Room has readied.")
        }
    }

    class RoomStartState(override val room: Room) : RoomState(room) {
        override fun enter() {
            val reqData = ReqData.GameStartDTO().apply {
                this.roomId = room.id
            }
            Dispatcher.dispatch(reqData)

            room.updateRound()




            logger.trace("Room is starting.")
        }

        override fun exit() {
            room.transitionTo(RoomDealPlayerCardsState(room))
            logger.trace("Room has started.")
        }
    }

    class RoomDealPlayerCardsState(override val room: Room) : RoomState(room) {
        override fun enter() {
            val reqData = ReqData.GameDealPlayerCardsDTO().apply {
                this.roomId = room.id
            }
            Dispatcher.dispatch(reqData)
            exit()
            logger.trace("Room is dealing player cards.")
        }

        override fun exit() {
            room.transitionTo(RoomBettingState(room))
            logger.trace("Room has dealt player cards.")
        }
    }

    class RoomBettingState(override val room: Room) : RoomState(room) {

        override fun enter() {
            logger.trace("Room is betting.")
        }

        override fun update() {
            if (checkBetsEqual()) {
                exit()
            } else {
                continueCurrentRound()
            }
        }

        private fun continueCurrentRound() {
            // 这里填写你的处理代码
            if (room.nextToBetPlayer == null) {
                room.nextToBetPlayer = room.getPlayerList().first()
            }
            room.nextToBetPlayer?.transitionTo(PlayerState.PlayerBettingState(room.nextToBetPlayer!!))
        }

        private fun checkBetsEqual(): Boolean {
            val maxBet = room.getPlayerList().maxOfOrNull { it.betMap.values.sum() }
            return room.getPlayerList().all { player -> player.betMap.values.sum() == maxBet }
        }

        override fun exit() {
            // 如果最后一轮是玩家卡牌发牌状态，那么转换到转牌发牌状态
            if (room.lastStateName == RoomDealPlayerCardsState::class.java.simpleName) {
                room.transitionTo(RoomDealTurnCardState(room))
            }
            logger.trace("Room has bet.")
        }
    }

    class RoomDealFlopCardsState(override val room: Room) : RoomState(room) {
        override fun enter() {
            logger.trace("Room is dealing flop cards.")
        }

        override fun exit() {
            logger.trace("Room has dealt flop cards.")
        }
    }

    class RoomDealTurnCardState(override val room: Room) : RoomState(room) {
        override fun enter() {
            logger.trace("Room is dealing turn card.")
        }

        override fun exit() {
            logger.trace("Room has dealt turn card.")
        }
    }

    class RoomDealRiverCardState(override val room: Room) : RoomState(room) {
        override fun enter() {
            logger.trace("Room is dealing river card.")
        }

        override fun exit() {
            logger.trace("Room has dealt river card.")
        }
    }

    class RoomEndState(override val room: Room) : RoomState(room) {
        override fun enter() {
            logger.trace("Room is ending.")
        }

        override fun exit() {
            logger.trace("Room has ended.")
        }
    }
}