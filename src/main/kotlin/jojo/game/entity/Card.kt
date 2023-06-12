package jojo.game.entity

import jojo.game.enums.CardColor

data class Card(
    val id: Int,
    val value: Int,
    val color: CardColor
)
