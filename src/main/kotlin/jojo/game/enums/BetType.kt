package jojo.game.enums

import javax.naming.NameNotFoundException

enum class BetType {
    NONE,
    FOLD,
    CHECK,
    CALL,
    RAISE,
    ALL_IN;

    companion object {
        fun byName(name: String): BetType {
            return BetType.values().find { it.name == name } ?: throw NameNotFoundException()
        }
    }
}