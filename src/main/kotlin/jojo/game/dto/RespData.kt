package jojo.game.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jojo.game.enums.DataType


@JsonIgnoreProperties(ignoreUnknown = true)
sealed class RespData {
    open var type: DataType = DataType.DEFAULT

    data class LoginDTO(
        override var type: DataType = DataType.LOGIN,
        var username: String = "",
        var playerId: String = ""
    ) : RespData()

    data class RoomCreateDTO(
        override var type: DataType = DataType.ROOM_CREATE,
    ) : RespData()

    data class RoomJoinDTO(
        override var type: DataType = DataType.ROOM_JOIN,
        var roomId: String = ""
    ) : RespData()

    data class RoomLeaveDTO(
        override var type: DataType = DataType.ROOM_LEAVE,
    ) : RespData()

    data class RoomSearchDTO(
        override var type: DataType = DataType.ROOM_SEARCH,
        var id: String = "",
        var name: String = ""
    ) : RespData()

    data class BetDTO(
        override var type: DataType = DataType.GAME_BET,
        var username: String = "",
        var password: String = ""
    ) : RespData()
}