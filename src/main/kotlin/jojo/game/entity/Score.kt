package jojo.game.entity

import java.math.BigDecimal

data class Score(
    var value: BigDecimal = BigDecimal.ZERO,
)  {
    fun addScore(value: BigDecimal) {
        this.value += value
    }

    fun subtractScore(value: BigDecimal) {
        this.value -= value
    }
}
