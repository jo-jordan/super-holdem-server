package jojo.game.controller

import jojo.game.dto.ReqData
import org.java_websocket.WebSocket

/**
 * Game controller
 */
class GameController {

    fun ready(gameReadyDTO: ReqData.GameReadyDTO, conn: WebSocket?) {
        // TODO
    }

    fun start(param: Any, conn: WebSocket?) {
        // TODO
    }

    fun dealPlayerCards(gameDealPlayerCardsDTO: ReqData.GameDealPlayerCardsDTO, conn: WebSocket?) {
        // TODO
    }

    fun dealFlopCards(gameDealFlopCardsDTO: ReqData.GameDealFlopCardsDTO, conn: WebSocket?) {
        // TODO
    }

    fun dealTurnCard(gameDealTurnCardsDTO: ReqData.GameDealTurnCardsDTO, conn: WebSocket?) {
        // TODO
    }

    fun dealRiverCard() {
        // TODO
    }

    fun bet() {
        // TODO
    }

    fun calculateWinner() {
        // TODO
    }
}