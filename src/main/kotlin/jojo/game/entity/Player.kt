package jojo.game.entity

import jojo.game.enums.CardType
import jojo.game.enums.Position
import jojo.game.global.GameGlobal
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
    var isFold = false
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

    fun snapshot(): Map<String, String> {
        return hashMapOf(
            "id" to id,
            "name" to name,
            "cardList" to cardList.joinToString { "${it.id}" }, //
            "ready" to ready.toString(),
            "score" to score.toString(),
            "position" to position.name,
            "nextPlayer" to (nextPlayer?.id ?: ""),
            "room" to (room?.id ?: ""),
            "maxCardType" to maxCardType.toString(),
            "cardsToCalculate" to cardsToCalculate.map {it.id }.joinToString { "," },
            "isFold" to isFold.toString()
        ).filterValues { it != "" && it != "null" && it != "[]" }
    }

    companion object {
        fun fromCache(map: Map<String, String>): Player {
            var player = GameGlobal.playerMap[map.getValue("id")]
            if (player == null) {
                player = Player()
            }
            player.name = map.getValue("name")
            player.id = map.getValue("id")
            player.room = GameGlobal.roomMap[map.getValue("room")]
            player.nextPlayer = GameGlobal.playerMap[map.getValue("nextPlayer")]
            player.isFold = map.getValue("isFold").toBoolean()
//        player.position = map.getValue("position")

            return player
        }
    }
}