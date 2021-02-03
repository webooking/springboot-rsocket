package org.study.account.controller

import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.study.account.model.User
import org.study.account.service.UserService

@Controller
class UserController(val userService: UserService) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @MessageMapping("create.the.user")
    suspend fun create(request: User.Create) {
        log.info("create a user, request parameters: {}", request)
        userService.create(request.toDto())
    }

}