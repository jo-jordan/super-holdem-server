package jojo.game.controller

import jojo.game.dto.ReqData
import jojo.game.dto.RespData
import jojo.game.entity.Room
import jojo.game.global.GameGlobal
import jojo.game.utils.JacksonUtils
import org.java_websocket.WebSocket
import java.util.UUID

class RoomController {
    fun creatRoom(param: ReqData.RoomCreateDTO, conn: WebSocket?) {
        val id = UUID.randomUUID().toString()
        GameGlobal.roomMap[id] = Room().apply {
            this.name = param.name
        }
    }

    fun joinRoom(param: ReqData.RoomJoinDTO, conn: WebSocket?) {
        if (GameGlobal.roomMap.contains(param.roomId)) {
            if (GameGlobal.playerMap.contains(param.playerId)) {
                val player = GameGlobal.playerMap.getValue(param.playerId)
                val room = GameGlobal.roomMap.getValue(param.roomId)
                player.room = room
                room.addPlayer(player)
                conn?.send(JacksonUtils.objectMapper.writeValueAsString(
                    RespData.RoomJoinDTO().apply {
                        this.roomId = room.id
                    })
                )
            }
        }
    }

    fun leaveRoom(param: ReqData.RoomLeaveDTO, conn: WebSocket?) {
        if (GameGlobal.roomMap.contains(param.roomId)) {
            val room = GameGlobal.roomMap.getValue(param.roomId)
            room.removePlayerById(param.playerId)
            if (room.getPlayerList().isEmpty()) {
                GameGlobal.roomMap.remove(param.roomId)
            }
        }
    }

    fun searchRoom(param: ReqData.RoomSearchDTO, conn: WebSocket?) {
        val roomList = GameGlobal.roomMap.values.filter { it.name.contains(param.keyword) }.map {
            RespData.RoomSearchDTO().apply {
                id = it.id
                name = it.name
            }
        }

        conn?.send(JacksonUtils.objectMapper.writeValueAsString(roomList))
    }
}