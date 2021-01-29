package org.study.account.controller

import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.study.account.model.User
import org.study.account.service.UserService

@Controller
class UserController(val userService: UserService) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @MessageMapping("request-response")
    suspend fun requestResponse(name: String): User {
        log.info("Received request-response request: {}", name)
        val user = userService.findUserByName(name).awaitFirst()

        log.info("request-response Response: {}", user)
        return user
    }
}