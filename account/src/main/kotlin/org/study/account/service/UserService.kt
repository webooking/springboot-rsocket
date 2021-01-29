package org.study.account.service

import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Service
import org.study.account.model.User
import reactor.core.publisher.Mono

@Service
class UserService(val users: List<User>) {

    suspend fun findUserByName(name: String): Mono<User> = mono {
        users.first { it.name == name }
    }
}