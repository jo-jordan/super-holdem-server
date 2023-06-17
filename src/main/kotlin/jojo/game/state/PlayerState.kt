package jojo.game.state

import jojo.game.core.Dispatcher
import jojo.game.dto.ReqData
import jojo.game.entity.Player
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.concurrent.timer

sealed class PlayerState(open val player: Player) {

    val logger: Logger = LoggerFactory.getLogger("PlayerState")

    protected open var period: Long = 5000L

    protected open var timer: Timer = Timer()
    open fun enter() {
        timer = timer("PlayerStateTimer", false, 0, period) {
            exit()
        }
    }
    open fun exit() {
        timer.cancel()
        timer.purge()
    }

    open fun update() {}

    class PlayerEnterRoomState(override val player: Player) : PlayerState(player) {

        override var period: Long = 1500L

        override fun enter() {
            logger.trace("Player[{}] is entering the room.", player.id)
            super.enter()
        }

        override fun exit() {
            super.exit()

            if (player.ready) {
                logger.trace("Player[{}] has entered the room.", player.id)
            } else {
                player.room?.removePlayer(player)
                player.room = null
            }
        }
    }

    class PlayerReadyState(override val player: Player) : PlayerState(player) {
        override fun enter() {
            player.ready = true
            logger.trace("Player[{}] is ready.", player.id)
        }

        override fun exit() {
            logger.trace("Player[{}] has readied.", player.id)
        }
    }

    class PlayerWaitingState(override val player: Player) : PlayerState(player) {
        override fun enter() {
            // room must be RoomBettingState
            player.room?.lastBetPlayer = player
            player.room?.nextToBetPlayer = player.nextPlayer

            println("Player is waiting.")
        }

        override fun exit() {
            println("Player is done waiting.")
        }

    }

    class PlayerBettingState(override val player: Player) : PlayerState(player) {
        override fun enter() {
            val reqData = ReqData.GameUpdateBetDTO().apply {
                this.roomId = player.room?.id ?: ""
                this.playerId = player.id
            }
            Dispatcher.dispatch(reqData)
            println("Player is betting.")
        }

        override fun exit() {
            println("Player has placed a bet of.")
        }
    }

}
