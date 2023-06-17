package jojo.game.core

import jojo.game.global.GameGlobal

abstract class Controller {

    protected fun sendTo(roomId: String, receiverId: String, message: String) {
        GameGlobal.roomConnections[roomId]?.get(receiverId)?.send(message)
    }

    protected fun broadcast(roomId: String, sender: String, message: String, includeSender: Boolean = false) {
        GameGlobal.roomConnections[roomId]
            ?.filter { includeSender || it.key != sender }
            ?.forEach {
                it.value?.send(message)
            }
    }
}