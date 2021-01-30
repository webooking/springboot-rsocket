package org.study.account.controller

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.study.account.model.User
import org.study.account.service.UserService
import java.util.*

@Controller
class UserController(val userService: UserService) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @MessageMapping("request-response")
    suspend fun requestResponse(name: String): User {
        log.info("Received request-response request: {}", name)
        return userService.findUserByName(name)
    }

    @MessageMapping("fireAndForget")
    suspend fun fireAndForget(model: User) {
        log.info("Received fireAndForget request: {}", model)
        userService.save(model)
    }

    @MessageMapping("stream")
    suspend fun stream() = userService.finAllUsers()

    @MessageMapping("channel")
    suspend fun channel(clientFlow: Flow<String>): Flow<String> {
        clientFlow
            .take(2)
            .collect {
                log.info("Received from the client: $it")
            }

        return flow {
            repeat(10) {
                delay(1000)
                val message = "Send by the server: ${UUID.randomUUID()}"
                log.info("Before ----- $message")
                emit(message)
            }
        }
    }
}