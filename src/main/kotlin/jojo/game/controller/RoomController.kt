package jojo.game.controller

import jojo.game.core.Controller
import jojo.game.dto.ReqData
import jojo.game.dto.RespData
import jojo.game.entity.Room
import jojo.game.global.GameGlobal
import jojo.game.state.RoomState
import jojo.game.utils.JacksonUtils
import org.java_websocket.WebSocket
import org.slf4j.LoggerFactory

class RoomController: Controller() {
    private val logger = LoggerFactory.getLogger("RoomController")
    fun creatRoom(param: ReqData.RoomCreateDTO, conn: WebSocket?) {
        val room = Room().apply {
            this.name = param.name
        }
        GameGlobal.roomMap[room.id] = room

        room.addPlayer(GameGlobal.playerMap.getValue(param.playerId))
        room.addPlayerConnection(param.playerId, conn)

        room.transitionTo(RoomState.RoomReadyState(room))

        logger.info("player ${param.playerId} created room: ${room.id}")
        conn?.send(
            JacksonUtils.objectMapper.writeValueAsString(
                RespData.RoomCreateDTO().apply {
                    this.roomInfo = room.getRoomInfo(param.playerId)
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

                logger.info("player ${param.playerId} joined room: ${room.id}")
                sentRoomInfoToAll(room)
            }
        }
    }

    fun leaveRoom(param: ReqData.RoomLeaveDTO) {
        if (GameGlobal.roomMap.contains(param.roomId)) {
            val room = GameGlobal.roomMap.getValue(param.roomId)
            room.removePlayerById(param.playerId)
            room.removePlayerConnection(param.playerId)
            if (room.getPlayerList().isEmpty()) {
                GameGlobal.roomMap.remove(param.roomId)
            }

            logger.info("player ${param.playerId} left room: ${room.id}")
            sentRoomInfoToAll(room)
        }
    }

    fun searchRoom(param: ReqData.RoomSearchDTO, conn: WebSocket?) {
        val result = RespData.RoomSearchDTO().apply {
            this.roomList = GameGlobal.roomMap.values.filter { it.name.contains(param.keyword) }.map {
                RespData.RoomInfoDTO().apply {
                    roomId = it.id
                    roomName = it.name
                }
            }
        }

        conn?.send(JacksonUtils.objectMapper.writeValueAsString(result))
    }
}