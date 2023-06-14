package jojo.game.state

import jojo.game.entity.Room
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
        }
    }

    class RoomWaitingState(override val room: Room) : RoomState(room) {
        override fun enter() {
            println("Room is waiting.")
        }

        override fun exit() {
            println("Room is done waiting.")
        }
    }

    class RoomEndState(override val room: Room) : RoomState(room) {
        override fun enter() {
            println("Room is ending.")
        }

        override fun exit() {
            println("Room has ended.")
        }
    }
}