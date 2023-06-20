package jojo.game.state

import jojo.game.core.Dispatcher
import jojo.game.dto.ReqData
import jojo.game.entity.Player
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

sealed class PlayerState(open val player: Player) {

    val logger: Logger = LoggerFactory.getLogger("PlayerState")

    protected open var period: Long = 5000L

    protected open var timer: Timer = Timer()
    open fun enter() {

    }
    open fun exit() {

    }

    open fun update() {}

    class PlayerEnterRoomState(override val player: Player) : PlayerState(player) {

        override var period: Long = 1500L

        override fun enter() {
            logger.info("Player[{}] is entering the room.", player.id)
            super.enter()
        }

        override fun exit() {
            super.exit()

            if (player.ready) {
                logger.info("Player[{}] has entered the room.", player.id)
            } else {
                player.room?.removePlayer(player)
                player.room = null
            }
        }
    }

    class PlayerReadyState(override val player: Player) : PlayerState(player) {
        override fun enter() {
            player.ready = true
            logger.info("Player[{}, {}] is ready.", player.name, player.id)
        }

        override fun exit() {
            logger.info("Player[{}, {}] has readied.", player.name, player.id)
        }
    }

    class PlayerWaitingState(override val player: Player) : PlayerState(player) {
        override fun enter() {
            // room must be RoomBettingState
            logger.info("Player[{}, {}] is waiting.", player.name, player.id)
        }

        override fun exit() {
            logger.info("Player[{}, {}] has waited.", player.name, player.id)
        }

    }

    class PlayerBettingState(override val player: Player) : PlayerState(player) {
        override fun enter() {
            val reqData = ReqData.GameUpdateBetDTO().apply {
                this.roomId = player.room?.id ?: ""
                this.playerId = player.id
            }
            Dispatcher.dispatch(reqData)
            logger.info("Player[{}, {}] is betting.", player.name, player.id)
        }

        override fun exit() {
            logger.info("Player[{}, {}] has bet.", player.name, player.id)
        }
    }

}
