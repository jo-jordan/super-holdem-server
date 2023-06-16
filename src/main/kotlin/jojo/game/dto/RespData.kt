package jojo.game.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jojo.game.entity.Card
import jojo.game.enums.DataType


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = RespData.LoginDTO::class, name = "LOGIN"),
    JsonSubTypes.Type(value = RespData.PlayerInfoDTO::class, name = "PLAYER_INFO"),
    JsonSubTypes.Type(value = RespData.RoomInfoDTO::class, name = "ROOM_INFO"),
    JsonSubTypes.Type(value = RespData.RoomCreateDTO::class, name = "ROOM_CREATE"),
    JsonSubTypes.Type(value = RespData.RoomJoinDTO::class, name = "ROOM_JOIN"),
    JsonSubTypes.Type(value = RespData.RoomLeaveDTO::class, name = "ROOM_LEAVE"),
    JsonSubTypes.Type(value = RespData.RoomSearchDTO::class, name = "ROOM_SEARCH"),
    JsonSubTypes.Type(value = RespData.GameReadyDTO::class, name = "GAME_READY"),
    JsonSubTypes.Type(value = RespData.GameStartDTO::class, name = "GAME_START"),
    JsonSubTypes.Type(value = RespData.GameDealPlayerCardsDTO::class, name = "GAME_DEAL_PLAYER_CARDS"),
    JsonSubTypes.Type(value = RespData.GameDealFlopCardsDTO::class, name = "GAME_DEAL_FLOP_CARDS"),
    JsonSubTypes.Type(value = RespData.GameDealTurnCardsDTO::class, name = "GAME_DEAL_TURN_CARDS"),
    JsonSubTypes.Type(value = RespData.GameDealRiverCardsDTO::class, name = "GAME_DEAL_RIVER_CARDS"),
    JsonSubTypes.Type(value = RespData.GameUpdateBetDTO::class, name = "GAME_UPDATE_BET"),
    JsonSubTypes.Type(value = RespData.BetDTO::class, name = "GAME_BET"),
)
sealed class RespData {
    abstract var type: DataType
    open var playerId : String = ""
    open var roomId : String = ""

    data class LoginDTO(
        override var type: DataType = DataType.LOGIN,
        var username: String = "",
    ) : RespData()

    data class RoomCreateDTO(
        override var type: DataType = DataType.ROOM_CREATE,
        var roomName: String = "",
    ) : RespData()

    data class RoomJoinDTO(
        override var type: DataType = DataType.ROOM_JOIN,
    ) : RespData()

    data class RoomLeaveDTO(
        override var type: DataType = DataType.ROOM_LEAVE,
    ) : RespData()

    data class RoomSearchDTO(
        override var type: DataType = DataType.ROOM_SEARCH,
        var roomList: List<RoomCreateDTO> = listOf(),
    ) : RespData()

    data class PlayerInfoDTO(
        override var type: DataType = DataType.PLAYER_INFO,
        var username: String = "",
        var betAmount: Int = 0,
        var cardList: List<Card> = listOf(),
    ) : RespData()

    data class RoomInfoDTO(
        override var type: DataType = DataType.ROOM_INFO,
        var roomName: String = "",
        var playerInfoList: List<PlayerInfoDTO> = listOf(),
        var betAmount: Int = 0,
    ) : RespData()

    data class GameReadyDTO(
        override var type: DataType = DataType.GAME_READY,
    ) : RespData()

    data class GameStartDTO(
        override var type: DataType = DataType.GAME_START,
    ) : RespData()

    data class GameDealPlayerCardsDTO(
        override var type: DataType = DataType.GAME_DEAL_PLAYER_CARDS,
        var cards: List<String> = listOf(),
        var roomInfo: RoomInfoDTO? = null,
    ) : RespData()

    data class GameDealFlopCardsDTO(
        override var type: DataType = DataType.GAME_DEAL_FLOP_CARDS,
        var cards: List<String> = listOf(),
    ) : RespData()

    data class GameDealTurnCardsDTO(
        override var type: DataType = DataType.GAME_DEAL_TURN_CARDS,
        var cards: List<String> = listOf(),
    ) : RespData()

    data class GameDealRiverCardsDTO(
        override var type: DataType = DataType.GAME_DEAL_RIVER_CARDS,
        var cards: List<String> = listOf(),
    ) : RespData()

    data class GameUpdateBetDTO(
        override var type: DataType = DataType.GAME_UPDATE_BET,
        var roomInfo: RoomInfoDTO = RoomInfoDTO(),
        var bet: Int = 0,
    ) : RespData()

    data class BetDTO(
        override var type: DataType = DataType.GAME_BET,
    ) : RespData()
}