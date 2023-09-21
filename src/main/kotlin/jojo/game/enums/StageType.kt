package jojo.game.enums

import javax.naming.NameNotFoundException

enum class StageType {
    BET_AFTER_DEAL_PLAYER_CARDS,
    BET_AFTER_DEAL_FLOP_CARDS,
    BET_AFTER_DEAL_TURN_CARDS,
    BET_AFTER_DEAL_RIVER_CARDS,
    RESULT;

    companion object {
        fun byName(name: String): StageType {
            return StageType.values().find { it.name == name } ?: throw NameNotFoundException()
        }
    }

}