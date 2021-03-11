package org.study.auth.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.getAndAwait
import org.springframework.stereotype.Service
import org.study.auth.model.API
import org.study.auth.model.Client
import org.study.auth.model.request.ClientRequest
import reactor.kotlin.core.publisher.toFlux

@Service
class ClientService(val clientRedisTemplate: ReactiveRedisTemplate<String, Client>) {
    private val clientNamePrefix = "auth:client"
    private val clientNames = listOf("account", "order")

    suspend fun save(): Flow<Boolean> = clientNames.toFlux().map { clientName ->
        when (clientName) {
            "account" -> Client(
                name = clientName,
                apiList = listOf(
                    API.Generate_Token,
                    API.Delete_Token,
                    API.Get_Authentication,
                    API.Update_Authentication,
                    API.Refresh_Token,
                )
            )
            else -> Client(clientName)
        }
    }.flatMap { client ->
        clientRedisTemplate.opsForValue().set("${clientNamePrefix}:${client.name}", client)
    }.asFlow()

    suspend fun findAll(): Flow<Client> = clientNames.toFlux()
        .map { clientName ->
            "${clientNamePrefix}:${clientName}"
        }.flatMap {
            clientRedisTemplate.opsForValue().get(it)
        }.asFlow()

    suspend fun findByName(clientName: String): Client =
        clientRedisTemplate.opsForValue().getAndAwait("${clientNamePrefix}:${clientName}")!!

    suspend fun clear(): Flow<Boolean> = clientNames.toFlux()
        .map {
            "${clientNamePrefix}:${it}"
        }.flatMap {
            clientRedisTemplate.opsForValue().delete(it)
        }.asFlow()

    suspend fun verify(client: ClientRequest, api: String): Boolean = findByName(client.name).let {
        val flag = it.id == client.id && it.secret == client.secret && it.apiList.contains(api)
        if (!flag) {
            throw org.springframework.security.access.AccessDeniedException("Access Denied")
        }
        flag
    }
}