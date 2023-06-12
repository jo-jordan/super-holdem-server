package jojo.game.entity

import jojo.game.enums.CardType
import jojo.game.enums.Position
import jojo.game.state.PlayerState
import jojo.game.utils.CardUtils

class Player {
    var state: PlayerState = PlayerState.PlayerWaitingState()

    var id: Int = 0
    var name: String = ""
    var cardList: List<Card> = mutableListOf()
    var score: Score = Score()
    var seatType: Position = Position.NONE
    var nextPlayer: Player? = null
    var room: Room? = null
    var maxCardType = Pair(CardType.NONE, emptyList<Card>())
    var cardsToCalculate = emptyList<Card>()

    fun transitionTo(state: PlayerState) {
        this.state.exit()
        state.enter()
        this.state = state
    }

    fun calculateMaxCardType(): Pair<CardType, List<Card>> {
        val cardsOnTable: List<Card> = room?.cardList ?: emptyList()
        val cardsInHand: List<Card> = cardList

        this.cardsToCalculate = cardsOnTable + cardsInHand
        this.maxCardType = CardUtils.calculateCardType(this.cardsToCalculate)

        return this.maxCardType
    }
}