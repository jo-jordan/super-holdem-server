package jojo.game.state

import kotlin.concurrent.timer

sealed class PlayerState {

    protected open var period: Long = 5000L
    open fun enter() {
        timer("PlayerStateTimer", false, 0, period) {
            exit()
        }
    }
    open fun exit() {}

    open fun update() {}

    class PlayerEnterRoomState : PlayerState() {

        override var period: Long = 15000L

        override fun enter() {
            println("Player is entering the room.")
        }

        override fun exit() {
            println("Player has entered the room.")
        }

        override fun update() {
            println("Player is updating.")
        }
    }

    class PlayerWaitingState : PlayerState() {
        override fun enter() {
            println("Player is waiting.")
        }

        override fun exit() {
            println("Player is done waiting.")
        }

    }

    class PlayerBettingState(val bet: Int) : PlayerState() {
        override fun enter() {
            println("Player is betting $bet.")
        }

        override fun exit() {
            println("Player has placed a bet of $bet.")
        }
    }

    class PlayerWaitingForOthersState : PlayerState() {
        override fun enter() {
            println("Player is waiting for others.")
        }

        override fun exit() {
            println("Player is done waiting for others.")
        }
    }
}
