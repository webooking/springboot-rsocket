package org.study.account.controller

import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.study.account.model.User
import reactor.core.publisher.Mono

@Controller
class UserController(val users: List<User>) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @MessageMapping("request-response")
    fun requestResponse(name: String): Mono<User> {
        log.info("Received request-response request: {}", name)
        val user = users.first { it.name == name }
        log.info("request-response Response: {}", user)
        return Mono.just(user)
    }
}