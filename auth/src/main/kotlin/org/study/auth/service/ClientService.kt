package org.study.auth.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import org.study.auth.model.API
import org.study.auth.model.Client
import reactor.core.publisher.Flux

@Service
class ClientService(val clientRedisTemplate: ReactiveRedisTemplate<String, Client>) {
    suspend fun save(): Flow<Boolean> {
        val account = Client(
            name = "account",
            apiList = listOf(
                API.GENERATE_TOKEN,
                API.DELETE_TOKEN,
                API.GET_AUTHENTICATION,
                API.UPDATE_AUTHENTICATION,
                API.REFRESH_TOKEN,
            )
        )

        val order = Client("order")

        return Flux.just(account, order).flatMap {
            clientRedisTemplate.opsForValue().set("auth:client:${it.name}", it)
        }.asFlow()
    }
}