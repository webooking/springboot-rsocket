package org.study.feign

interface UserService {
    suspend fun sayHello(name: String): String
    suspend fun others(): Unit
}
