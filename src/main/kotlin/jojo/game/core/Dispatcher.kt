package jojo.game.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jojo.game.controller.LoginController
import jojo.game.dto.ReqData
import jojo.game.enums.DataType
import jojo.game.utils.JacksonUtils
import org.java_websocket.WebSocket

object Dispatcher {

    val loginController: LoginController = LoginController()
    fun dispatch(data: String?, conn: WebSocket?) {
        val dtoData = JacksonUtils.objectMapper.readValue(data, ReqData::class.java)
        when (dtoData.type)
        {
            DataType.LOGIN -> {
                loginController.login(dtoData as ReqData.LoginDTO, conn)
            }
            DataType.BET -> {
                println("Bet")
            }
            else -> {
                println("Default")
            }
        }
    }
}