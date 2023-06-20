package jojo.game.utils

import jojo.game.entity.Card
import jojo.game.entity.Player
import jojo.game.enums.CardColor
import jojo.game.enums.CardType
import jojo.game.enums.CardValue

object CardUtils {

    // All 52 cards in the game
    private var cardList = mutableListOf<Card>()

    // Initialize the card list
    fun init() {
        cardList.clear()
        var cardId = 1
        for (color in CardColor.values()) {
            // for Ace
            val ace = Card(cardId, 14, color)
            cardList.add(ace)
            cardId++

            // for 2-13
            for (value in 2..13) {
                val card = Card(cardId, value, color)
                cardList.add(card)
                cardId++
            }
        }
        shuffle()
    }


    fun shuffle() {
        cardList.shuffle()
    }

    fun getCardList(): List<Card> {
        return cardList
    }

    fun dealCard(): Card {
        return cardList.removeAt(0)
    }

    fun dealNullCard(): Card {
        return Card(-1, -1, CardColor.SPADE)
    }

    fun calculateCardType(cards: List<Card>): Pair<CardType, List<Card>> {
        return when {
            isRoyalStraightFlush(cards) -> {
                Pair(CardType.ROYAL_STRAIGHT_FLUSH, getRoyalStraightFlush(cards))
            }
            isStraightFlush(cards) -> {
                Pair(CardType.STRAIGHT_FLUSH, getStraightFlush(cards))
            }
            isFourOfAKind(cards) -> {
                Pair(CardType.FOUR_OF_A_KIND, getFourOfAKind(cards))
            }
            isFullHouse(cards) -> {
                Pair(CardType.FULL_HOUSE, getFullHouse(cards))
            }
            isFlush(cards) -> {
                Pair(CardType.FLUSH, getFlush(cards))
            }
            isStraight(cards) -> {
                Pair(CardType.STRAIGHT, getStraight(cards))
            }
            isThreeOfAKind(cards) -> {
                Pair(CardType.THREE_OF_A_KIND, getThreeOfAKind(cards))
            }
            isTwoPair(cards) -> {
                Pair(CardType.TWO_PAIR, getTwoPair(cards))
            }
            isOnePair(cards) -> {
                Pair(CardType.ONE_PAIR, getOnePair(cards))
            }
            else -> {
                Pair(CardType.HIGH_CARD, getHighCard(cards))
            }
        }
    }

    private fun isRoyalStraightFlush(cards: List<Card>): Boolean {
        val sortedCards = cards.sortedByDescending { it.value }
        return isStraightFlush(sortedCards) && sortedCards.first().value == 14 // Assume Ace's value is 14
    }

    private fun getRoyalStraightFlush(cards: List<Card>): List<Card> {
        val sortedCards = cards.sortedBy { it.value }
        return sortedCards.windowed(5).find { window ->
            val lastCard = window.last()
            // Check for royal straight flush (10-J-Q-K-A)
            lastCard.value == 14 && window.zipWithNext().all { (first, second) ->
                first.color == second.color && first.value + 1 == second.value
            }
        } ?: emptyList()
    }

    private fun isStraightFlush(cards: List<Card>): Boolean {
        if (cards.size < 5) {
            return false
        }
        val sortedCards = cards.sortedBy { it.value }
        return sortedCards.windowed(5).any { window ->
            val firstCard = window.first()
            val lastCard = window.last()
            // Check for 5-high straight flush (A-2-3-4-5)
            if (firstCard.value == CardValue.TWO.value && lastCard.value == CardValue.ACE.value) {
                return window.drop(1).zipWithNext().all { (first, second) ->
                    first.color == second.color && first.value + 1 == second.value
                }
            }
            // Otherwise check for regular straight flush
            window.zipWithNext().all { (first, second) ->
                first.color == second.color && first.value + 1 == second.value
            }
        }
    }

    private fun getStraightFlush(cards: List<Card>): List<Card> {
        val sortedCards = cards.sortedBy { it.value }
        return sortedCards.windowed(5).find { window ->
            // Check for straight flush (10-J-Q-K-A)
            window.zipWithNext().all { (first, second) ->
                first.color == second.color && first.value + 1 == second.value
            }
        } ?: emptyList()
    }

    private fun isFourOfAKind(cards: List<Card>): Boolean {
        val groupedCards = cards.groupBy { it.value }
        return groupedCards.any { (_, group) -> group.size == 4 }
    }

    private fun getFourOfAKind(cards: List<Card>): List<Card> {
        val groupedCards = cards.groupBy { it.value }
        val fourOfAKind = groupedCards.filter { (_, group) -> group.size == 4 }

        if (fourOfAKind.isNotEmpty()) {
            val fourCards = fourOfAKind.values.first()
            val remainingCards = cards.filter { it.value != fourCards.first().value }
            val highestRemainingCard = remainingCards.maxByOrNull { it.value }

            if (highestRemainingCard != null) {
                return fourCards + highestRemainingCard
            }
        }

        return emptyList()
    }

    private fun isFullHouse(cards: List<Card>): Boolean {
        val groupedCards = cards.groupBy { it.value }
        return groupedCards.any { (_, group) -> group.size == 3 } && groupedCards.any { (_, group) -> group.size == 2 }
    }

    private fun getFullHouse(cards: List<Card>): List<Card> {
        val groupedCards = cards.groupBy { it.value }
        val threeOfAKindGroup = groupedCards.filter { (_, group) -> group.size == 3 }
        val pairGroup = groupedCards.filter { (_, group) -> group.size == 2 && group != threeOfAKindGroup.values.first() }

        if (threeOfAKindGroup.isNotEmpty() && pairGroup.isNotEmpty()) {
            val threeCards = threeOfAKindGroup.values.first()
            val twoCards = pairGroup.values.first().take(2)
            return threeCards + twoCards
        }

        return emptyList()
    }

    private fun isFlush(cards: List<Card>): Boolean {
        val groupedCards = cards.groupBy { it.color }
        return groupedCards.any { (_, group) -> group.size >= 5 }
    }

    private fun getFlush(cards: List<Card>): List<Card> {
        val groupedCards = cards.groupBy { it.color }
        val flushGroup = groupedCards.filter { (_, group) -> group.size >= 5 }

        return if (flushGroup.isNotEmpty()) {
            flushGroup.values.first().sortedByDescending { it.value }.take(5)
        } else {
            emptyList()
        }
    }

    // 5, 11 12, 10, 12, 4, 6
    // 4 5 6 10 11 12 12
    private fun isStraight(cards: List<Card>): Boolean {
        if (cards.size < 5) return false
        val sortedCards = cards.sortedBy { it.value }.distinctBy { it.value }

        if (sortedCards.size >= 5) {
            // Check for a straight.
            if (sortedCards.zipWithNext().windowed(4).any { window ->
                    window.all { (first, second) -> first.value + 1 == second.value }
                }) return true

            // Check for a straight with ace as the lowest card (ace, 2, 3, 4, 5).
            if (sortedCards.first().value == 2 && sortedCards.last().value == 14) {
                val straightWithAceLow = listOf(Card(id = 0, value = 1, color = CardColor.SPADE)) + sortedCards
                if (straightWithAceLow.zipWithNext().windowed(4).any { window ->
                        window.all { (first, second) -> first.value + 1 == second.value }
                    }) return true
            }
        }

        return false
    }

    private fun getStraight(cards: List<Card>): List<Card> {
        if (cards.size < 5) return emptyList()
        val sortedCards = cards.sortedBy { it.value }.distinctBy { it.value }
        val possibleStraights = sortedCards.windowed(5).filter { it.zipWithNext().all { pair -> pair.first.value + 1 == pair.second.value } }
            .toMutableList()

        // Add check for the special case
        if (sortedCards.first().value == 2 && sortedCards.last().value == 14) {
            val aceAsOne = sortedCards.last().copy(value = 1)
            val newCards = listOf(aceAsOne) + sortedCards.dropLast(1)
            possibleStraights += newCards.windowed(5).filter { it.zipWithNext().all { pair -> pair.first.value + 1 == pair.second.value } }
        }

        return possibleStraights.maxByOrNull { it.last().value } ?: emptyList()
    }


    private fun isThreeOfAKind(cards: List<Card>): Boolean {
        val groupedCards = cards.groupBy { it.value }
        return groupedCards.any { (_, group) -> group.size == 3 }
    }

    private fun getThreeOfAKind(cards: List<Card>): List<Card> {
        val groupedCards = cards.groupBy { it.value }
        val threeOfAKindGroup = groupedCards.filter { (_, group) -> group.size == 3 }

        if (threeOfAKindGroup.isNotEmpty()) {
            val threeCards = threeOfAKindGroup.values.first()
            val remainingCards = cards.filter { it.value != threeCards.first().value }.sortedByDescending { it.value }
            return threeCards + remainingCards.take(2)
        }

        return emptyList()
    }

    private fun isTwoPair(cards: List<Card>): Boolean {
        val groupedCards = cards.groupBy { it.value }
        return groupedCards.count { (_, group) -> group.size == 2 } >= 2
    }

    private fun getTwoPair(cards: List<Card>): List<Card> {
        val groupedCards = cards.groupBy { it.value }
        val twoPairGroups = groupedCards.filter { (_, group) -> group.size == 2 }

        if (twoPairGroups.size >= 2) {
            val sortedGroups = twoPairGroups.values.sortedByDescending { it.first().value }
            val pairOne = sortedGroups[0]
            val pairTwo = sortedGroups[1]
            val remainingCards = cards.filter { it.value != pairOne.first().value && it.value != pairTwo.first().value }
            val highestRemainingCard = remainingCards.maxByOrNull { it.value }

            if (highestRemainingCard != null) {
                return pairOne + pairTwo + highestRemainingCard
            }
        }

        return emptyList()
    }

    private fun isOnePair(cards: List<Card>): Boolean {
        val groupedCards = cards.groupBy { it.value }
        return groupedCards.any { (_, group) -> group.size == 2 }
    }

    private fun getOnePair(cards: List<Card>): List<Card> {
        val groupedCards = cards.groupBy { it.value }
        val onePairGroup = groupedCards.filter { (_, group) -> group.size == 2 }

        if (onePairGroup.isNotEmpty()) {
            val pairCards = onePairGroup.values.first()
            val remainingCards = cards.filter { it.value != pairCards.first().value }.sortedByDescending { it.value }
            return pairCards + remainingCards.take(3)
        }

        return emptyList()
    }

    private fun getHighCard(cards: List<Card>): List<Card> {
        return cards.sortedByDescending { it.value }.take(5)
    }

    fun orderPlayerByCard(players: List<Player>): List<Player> {
        return players.sortedWith(compareByDescending<Player> { it.maxCardType.first.order }
            .thenByDescending { it.maxCardType.second.sumOf { v -> v.value } })
    }


}