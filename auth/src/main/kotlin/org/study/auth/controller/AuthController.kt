package org.study.auth.controller

import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class AuthController(val strTemplate: ReactiveStringRedisTemplate) {
    @MessageMapping("auth.generate.token")
    suspend fun generateToken(): String {
        val key = "access_token_001"
        val tokenValue = UUID.randomUUID().toString()
        strTemplate.opsForValue().set(key, tokenValue).awaitFirst()
        return strTemplate.opsForValue().get(key).awaitFirst()
    }
}