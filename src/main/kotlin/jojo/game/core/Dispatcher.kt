package jojo.game.core

import jojo.game.controller.GameController
import jojo.game.controller.RoomController
import jojo.game.dto.ReqData
import jojo.game.enums.DataType
import org.java_websocket.WebSocket
import org.slf4j.LoggerFactory

object Dispatcher {

    private val logger = LoggerFactory.getLogger("Dispatcher")

    private val roomController: RoomController = RoomController()

    private val gameController: GameController = GameController()

    fun dispatch(data: ReqData, conn: WebSocket? = null) {

        logger.info("{}: Message: {}", conn?.remoteSocketAddress?.address?.hostAddress, data)

        when (data.type)
        {
            DataType.ROOM_CREATE -> {
                roomController.creatRoom(data as ReqData.RoomCreateDTO, conn)
            }
            DataType.ROOM_JOIN -> {
                roomController.joinRoom(data as ReqData.RoomJoinDTO, conn)
            }
            DataType.ROOM_LEAVE -> {
                roomController.leaveRoom(data as ReqData.RoomLeaveDTO, conn)
            }
            DataType.ROOM_SEARCH -> {
                roomController.searchRoom(data as ReqData.RoomSearchDTO, conn)
            }
            DataType.GAME_READY -> {
                gameController.ready(data as ReqData.GameReadyDTO)
            }
            DataType.GAME_START -> {
                gameController.start(data as ReqData.GameStartDTO)
            }
            DataType.GAME_DEAL_PLAYER_CARDS -> {
                gameController.dealPlayerCards(data as ReqData.GameDealPlayerCardsDTO)
            }
            DataType.GAME_DEAL_FLOP_CARDS -> {
                gameController.dealFlopCards(data as ReqData.GameDealFlopCardsDTO)
            }
            DataType.GAME_DEAL_TURN_CARDS -> {
                gameController.dealTurnCard(data as ReqData.GameDealTurnCardsDTO)
            }
            DataType.GAME_DEAL_RIVER_CARDS -> {
                gameController.dealRiverCard(data as ReqData.GameDealRiverCardsDTO)
            }
            DataType.GAME_BET -> {
                gameController.bet(data as ReqData.GameBetDTO)
            }
            DataType.GAME_UPDATE_BET -> {
                gameController.updateBet(data as ReqData.GameUpdateBetDTO)
            }
            DataType.GAME_RESULT -> {
                gameController.result(data as ReqData.GameResultDTO)
            }
            else -> {
                println("Default")
            }
        }
    }
}