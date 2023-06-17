package jojo.game.controller

import jojo.game.dto.ReqData
import jojo.game.dto.RespData
import jojo.game.entity.Player
import jojo.game.global.GameGlobal
import jojo.game.utils.JacksonUtils
import java.util.*

class LoginController {
    fun login(param: ReqData.LoginDTO): String {

        // TODO check username and password

        val player = Player().apply {
            this.id = UUID.randomUUID().toString()
            this.name = param.username
        }
        GameGlobal.playerMap[player.id] = player

        return JacksonUtils.objectMapper.writeValueAsString(
            RespData.LoginDTO().apply {
                username = param.username
                playerId = player.id
            })
    }
}