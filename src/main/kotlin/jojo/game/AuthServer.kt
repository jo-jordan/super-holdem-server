package jojo.game

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import jojo.game.constant.CommonHeaders
import jojo.game.controller.LoginController
import jojo.game.dto.ReqData
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.concurrent.Executors


class AuthServer(private val port: Int) {

    private val logger = LoggerFactory.getLogger("AuthServer")

    private val server = HttpServer.create(InetSocketAddress(this.port), 0)

    fun start() {
        server.createContext("/", HealthCheckHandler())
        server.createContext("/login", LoginHandler())
        server.executor = Executors.newFixedThreadPool(1)
        server.start()
        logger.info("AuthServer started at port $port")
    }

    fun stop() {
        server.stop(0)
    }

    class HealthCheckHandler : HttpHandler {
        private val logger = LoggerFactory.getLogger("HealthCheckHandler")
        override fun handle(t: HttpExchange) {
            val response = "OK"
            t.sendResponseHeaders(200, response.length.toLong())

            val os = t.responseBody
            os.write(response.toByteArray())
            os.close()
            logger.info("HealthCheckHandler: Health check success.")
        }
    }

    class LoginHandler : HttpHandler {

        private val logger = LoggerFactory.getLogger("LoginHandler")

        private val loginController = LoginController()
        override fun handle(t: HttpExchange) {

            val username = t.requestHeaders.getFirst(CommonHeaders.HEADER_USERNAME)
            val password = t.requestHeaders.getFirst(CommonHeaders.HEADER_PASSWORD)

            val reqData = ReqData.LoginDTO().apply {
                this.username = username
                this.password = password
            }
            val response = loginController.login(reqData)

            t.sendResponseHeaders(200, response.length.toLong())

            val os = t.responseBody
            os.write(response.toByteArray())
            os.close()

            logger.info("LoginHandler: $username login success.")
        }
    }
}
