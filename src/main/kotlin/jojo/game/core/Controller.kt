package jojo.game.core

import jojo.game.dto.RespData
import jojo.game.entity.Room
import jojo.game.global.GameGlobal
import jojo.game.utils.JacksonUtils

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

    protected fun sentRoomInfoToAll(room: Room) {
        room.getPlayerList().forEach { p ->
            sendTo(room.id, p.id, JacksonUtils.objectMapper.writeValueAsString(
                RespData.RoomJoinDTO().apply {
                    val roomInfo = room.getRoomInfo(p.id)
                    this.roomInfo = roomInfo
                }
            ))
        }
    }
}