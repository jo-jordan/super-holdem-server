package jojo.game.entity

import jojo.game.enums.BetType

data class BetLog(val betRound: Int, val betType: BetType, val betAmount: Int, val time: Long = System.nanoTime())