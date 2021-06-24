package org.study.order.controller

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.study.order.AccountClient
import org.study.order.model.auth.Custom

@Controller
class OrderController(val client: AccountClient) {
    @MessageMapping("create.order")
    suspend fun createOrder(): Custom? {
//        val token = ReactiveSecurityContextHolder.getContext().map {
//            it.authentication.credentials as OAuth2AccessToken
//        }.awaitFirst()
        return client.findUser()
    }
}