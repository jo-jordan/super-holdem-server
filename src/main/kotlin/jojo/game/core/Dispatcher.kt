package jojo.game.core

import jojo.game.controller.GameController
import jojo.game.controller.RoomController
import jojo.game.dto.CommonData
import jojo.game.dto.ReqData
import jojo.game.enums.DataType
import jojo.game.utils.JacksonUtils
import org.java_websocket.WebSocket

object Dispatcher {

    private val roomController: RoomController = RoomController()

    private val gameController: GameController = GameController()

    fun dispatch(data: String?, conn: WebSocket?) {
        val commonData = conn?.getAttachment<CommonData>()
        val dtoData = JacksonUtils.objectMapper.readValue(data, ReqData::class.java).apply {
            this.playerId = commonData?.playerId ?: ""
            this.roomId = commonData?.roomId ?: ""
        }

        when (dtoData.type)
        {
            DataType.ROOM_CREATE -> {
                roomController.creatRoom(dtoData as ReqData.RoomCreateDTO, conn)
            }
            DataType.ROOM_JOIN -> {
                roomController.joinRoom(dtoData as ReqData.RoomJoinDTO, conn)
            }
            DataType.ROOM_LEAVE -> {
                roomController.leaveRoom(dtoData as ReqData.RoomLeaveDTO, conn)
            }
            DataType.ROOM_SEARCH -> {
                roomController.searchRoom(dtoData as ReqData.RoomSearchDTO, conn)
            }
            DataType.GAME_READY -> {
                gameController.ready(dtoData as ReqData.GameReadyDTO, conn)
            }
            DataType.GAME_START -> {
                gameController.start(dtoData as ReqData.GameStartDTO, conn)
            }
            DataType.GAME_DEAL_PLAYER_CARDS -> {
                gameController.dealPlayerCards(dtoData as ReqData.GameDealPlayerCardsDTO, conn)
            }
            DataType.GAME_DEAL_FLOP_CARDS -> {
                gameController.dealFlopCards(dtoData as ReqData.GameDealFlopCardsDTO, conn)
            }
            DataType.GAME_DEAL_TURN_CARDS -> {
                gameController.dealTurnCard(dtoData as ReqData.GameDealTurnCardsDTO, conn)
            }
            DataType.GAME_BET -> {
                println("Bet")
            }
            else -> {
                println("Default")
            }
        }
    }
}