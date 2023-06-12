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
    JsonSubTypes.Type(value = ReqData.BetDTO::class, name = "BET")
)
@JsonIgnoreProperties(ignoreUnknown = true)
sealed class ReqData {
    open var type: DataType = DataType.DEFAULT
    data class LoginDTO(
        override var type: DataType = DataType.LOGIN,
        var username: String = "",
        var password: String = ""
    ) : ReqData()

    data class RoomCreateDTO(
        override var type: DataType = DataType.ROOM_CREATE,
    ) : ReqData()

    data class RoomJoinDTO(
        override var type: DataType = DataType.ROOM_JOIN,
    ) : ReqData()

    data class RoomLeaveDTO(
        override var type: DataType = DataType.ROOM_LEAVE,
    ) : ReqData()

    data class RoomSearchDTO(
        override var type: DataType = DataType.ROOM_SEARCH,
    ) : ReqData()

    data class BetDTO(
        override var type: DataType = DataType.BET,
        var username: String = "",
        var password: String = ""
    ) : ReqData()
}