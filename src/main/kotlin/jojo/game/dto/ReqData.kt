package jojo.game.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jojo.game.enums.DataType

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = ReqData.LoginDTO::class, name = "LOGIN"),
    JsonSubTypes.Type(value = ReqData.RoomCreateDTO::class, name = "ROOM_CREATE"),
    JsonSubTypes.Type(value = ReqData.RoomJoinDTO::class, name = "ROOM_JOIN"),
    JsonSubTypes.Type(value = ReqData.RoomLeaveDTO::class, name = "ROOM_LEAVE"),
    JsonSubTypes.Type(value = ReqData.RoomSearchDTO::class, name = "ROOM_SEARCH"),
    JsonSubTypes.Type(value = ReqData.GameBetDTO::class, name = "GAME_BET")
)
@JsonIgnoreProperties(ignoreUnknown = true)
sealed class ReqData {
    open var type: DataType = DataType.DEFAULT
    open var playerId: String = ""
    open var roomId: String = ""
    data class LoginDTO(
        override var type: DataType = DataType.LOGIN,
        var username: String = "",
        var password: String = ""
    ) : ReqData()

    data class RoomCreateDTO(
        override var type: DataType = DataType.ROOM_CREATE,
        var name: String = ""
    ) : ReqData()

    data class RoomJoinDTO(
        override var type: DataType = DataType.ROOM_JOIN,
    ) : ReqData()

    data class RoomLeaveDTO(
        override var type: DataType = DataType.ROOM_LEAVE,

    ) : ReqData()

    data class RoomSearchDTO(
        override var type: DataType = DataType.ROOM_SEARCH,
        var keyword: String
    ) : ReqData()

    data class GameReadyDTO(
        override var type: DataType = DataType.GAME_READY,
    ) : ReqData()

    data class GameStartDTO(
        override var type: DataType = DataType.GAME_START,
    ) : ReqData()

    data class GameDealPlayerCardsDTO(
        override var type: DataType = DataType.GAME_DEAL_PLAYER_CARDS,
    ) : ReqData()

    data class GameDealFlopCardsDTO(
        override var type: DataType = DataType.GAME_DEAL_FLOP_CARDS,
    ) : ReqData()

    data class GameDealTurnCardsDTO(
        override var type: DataType = DataType.GAME_DEAL_TURN_CARDS,
    ) : ReqData()

    data class GameDealRiverCardsDTO(
        override var type: DataType = DataType.GAME_DEAL_RIVER_CARDS,
    ) : ReqData()

    data class GameResultDTO(
        override var type: DataType = DataType.GAME_RESULT,
    ) : ReqData()

    data class GameBetDTO(
        override var type: DataType = DataType.GAME_BET,
        var username: String = "",
        var password: String = ""
    ) : ReqData()
}