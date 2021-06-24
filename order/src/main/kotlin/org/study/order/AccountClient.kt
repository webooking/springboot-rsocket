package org.study.order

import kotlinx.coroutines.flow.Flow
import org.springframework.messaging.handler.annotation.MessageMapping
import org.study.feign.Anonymous
import org.study.feign.RSocketClient
import org.study.order.model.Custom
import org.study.order.model.auth.Custom as CustomDto

@RSocketClient
interface AccountClient {
    @Anonymous
    @MessageMapping("signUp")
    suspend fun signUp(): Unit

    @MessageMapping("find.user")
    suspend fun findUser(): CustomDto?

    @Anonymous
    @MessageMapping("stream")
    suspend fun receive(): Flow<Int>

    @Anonymous
    @MessageMapping("find.all")
    suspend fun findAll(): List<Custom.CreateRequest>
}
