package org.study.feign.controller

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.study.feign.client.UserService

@Controller
class UserController(val userService: UserService) {

    @MessageMapping("say.hello")
    suspend fun sayHello(name: String): String = userService.sayHello(name)

    @MessageMapping("others")
    suspend fun others(): Unit = userService.others()
}