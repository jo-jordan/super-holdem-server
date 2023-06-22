package jojo.game.entity

import jojo.game.enums.CardType
import jojo.game.enums.Position
import jojo.game.state.PlayerState
import jojo.game.utils.CardUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("Player")

class Player {
    var state: PlayerState? = null

    var id: String = ""
    var name: String = ""
    var cardList: List<Card> = mutableListOf()
    var ready: Boolean = false
    var score: Int = 0
    var position: Position = Position.NONE
    var nextPlayer: Player? = null
    var room: Room? = null
    var maxCardType = Pair(CardType.NONE, emptyList<Card>())
    var cardsToCalculate = emptyList<Card>()
    var isFold = true
    private var chipsAmount : Int = 100000

    private var betLog: List<BetLog> = mutableListOf()

    fun getBetLog(): List<BetLog> {
        return this.betLog
    }

    fun getChipsAmount(): Int {
        return this.chipsAmount
    }

    fun updateChips(amount: Int) {
        chipsAmount += amount
        logger.info("Player[{}, {}] chips amount: {}, diff: {}", name, id, chipsAmount, amount)
    }

    fun addBetLog(log: BetLog) {
        this.betLog += log
    }

    fun transitionTo(state: PlayerState) {
        if (this.state == null) {
            state.enter()
            this.state = state
            return
        }
        this.state?.exit()

        this.state = state
        state.enter()
    }

    fun updateState() {
        this.state?.update()
    }

    fun calculateMaxCardType(): Pair<CardType, List<Card>> {
        val cardsOnTable: List<Card> = room?.cards ?: emptyList()
        val cardsInHand: List<Card> = cardList

        this.cardsToCalculate = cardsOnTable + cardsInHand
        this.maxCardType = CardUtils.calculateCardType(this.cardsToCalculate)

        return this.maxCardType
    }

    fun reset() {
        cardList = mutableListOf()
        isFold = false
        chipsAmount = 100000
        betLog = mutableListOf()
        cardsToCalculate = emptyList()
        maxCardType = Pair(CardType.NONE, emptyList())
    }
}