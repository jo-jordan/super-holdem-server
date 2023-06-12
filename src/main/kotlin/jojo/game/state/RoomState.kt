package jojo.game.state

import kotlin.concurrent.timer

sealed class RoomState {

    protected open var period: Long = 5000L

    open fun enter() {
        timer("PlayerStateTimer", false, 0, period) {
            exit()
        }
    }
    open fun exit() {}

    open fun update() {}

    class RoomInitState : RoomState() {
        override fun enter() {
            println("Room is initializing.")
        }

        override fun exit() {
            println("Room has been initialized.")
        }
    }

    class RoomEndState : RoomState() {
        override fun enter() {
            println("Room is ending.")
        }

        override fun exit() {
            println("Room has ended.")
        }
    }
}