package jojo.game.enums

enum class Position(val alias: String, val type: String) {
    NONE("NONE", "NONE"),
    BUTTON("BTN", "LATE"),
    SMALL_BLIND("SB", "EARLY"),
    BIG_BLIND("BB", "EARLY"),
    UNDER_THE_GUN("UTG", "EARLY"),
    UNDER_THE_GUN_PLUS_ONE("UTG+1", "EARLY"),
    MID_POSITION("MP", "MIDDLE"),
    MID_POSITION_PLUS_ONE("MP+1", "MIDDLE"),
    HIJACK("HJ", "LATE"),
    CUT_OFF("CO", "LATE")
}