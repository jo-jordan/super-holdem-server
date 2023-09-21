package jojo.game.entity

data class GameConfig(
    var smallBlind: Int = 10,
    var bigBlind: Int = 20,
    var maxPlayer: Int = 9,
    var minPlayer: Int = 3,
    var maxRound: Int = 10,
    var numToStart: Int = 3,
) {

}
