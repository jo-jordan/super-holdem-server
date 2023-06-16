package jojo.game.controller

import jojo.game.core.Controller
import jojo.game.dto.ReqData
import jojo.game.dto.RespData
import jojo.game.entity.Room
import jojo.game.global.GameGlobal
import jojo.game.state.RoomState
import jojo.game.utils.JacksonUtils
import org.java_websocket.WebSocket

class RoomController: Controller() {
    fun creatRoom(param: ReqData.RoomCreateDTO, conn: WebSocket?) {
        val room = Room().apply {
            this.name = param.name
        }
        GameGlobal.roomMap[room.id] = room

        room.addPlayer(GameGlobal.playerMap.getValue(param.playerId))
        room.addPlayerConnection(param.playerId, conn)

        room.transitionTo(RoomState.RoomReadyState(room))

        conn?.send(
            JacksonUtils.objectMapper.writeValueAsString(
                RespData.RoomCreateDTO().apply {
                    this.roomId = room.id
                })
        )
    }

    fun joinRoom(param: ReqData.RoomJoinDTO, conn: WebSocket?) {
        if (GameGlobal.roomMap.contains(param.roomId)) {
            if (GameGlobal.playerMap.contains(param.playerId)) {
                val player = GameGlobal.playerMap.getValue(param.playerId)
                val room = GameGlobal.roomMap.getValue(param.roomId)
                player.room = room
                room.addPlayer(player)
                room.addPlayerConnection(param.playerId, conn)

                broadcast(param.playerId, room.id, JacksonUtils.objectMapper.writeValueAsString(room.getRoomInfo()))
            }
        }
    }

    fun leaveRoom(param: ReqData.RoomLeaveDTO, conn: WebSocket?) {
        if (GameGlobal.roomMap.contains(param.roomId)) {
            val room = GameGlobal.roomMap.getValue(param.roomId)
            room.removePlayerById(param.playerId)
            room.removePlayerConnection(param.playerId)
            if (room.getPlayerList().isEmpty()) {
                GameGlobal.roomMap.remove(param.roomId)
            }

            broadcast(param.playerId, room.id, JacksonUtils.objectMapper.writeValueAsString(room.getRoomInfo()))
        }
    }

    fun searchRoom(param: ReqData.RoomSearchDTO, conn: WebSocket?) {
        val result = RespData.RoomSearchDTO().apply {
            this.roomList = GameGlobal.roomMap.values.filter { it.name.contains(param.keyword) }.map {
                RespData.RoomCreateDTO().apply {
                    roomId = it.id
                    roomName = it.name
                }
            }
        }

        conn?.send(JacksonUtils.objectMapper.writeValueAsString(result))
    }
}