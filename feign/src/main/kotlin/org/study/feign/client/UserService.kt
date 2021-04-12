package org.study.feign.client

import org.springframework.messaging.handler.annotation.MessageMapping
import org.study.feign.annotation.Anonymous
import org.study.feign.annotation.RSocketClient

@RSocketClient
interface UserService {

    @Anonymous
    @MessageMapping("say.hello")
    suspend fun sayHello(name: String): String

    @MessageMapping("others")
    suspend fun others(): Unit
}
