package jojo.game.controller

import jojo.game.dto.ReqData
import jojo.game.dto.RespData
import jojo.game.utils.JacksonUtils
import org.java_websocket.WebSocket

class LoginController {
    fun login(param: ReqData.LoginDTO, conn: WebSocket?) {

        conn?.send(JacksonUtils.objectMapper.writeValueAsString(
            RespData.LoginDTO().apply {
                username = param.username
                password = param.password
            }))
    }
}