package org.study.account.service

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.study.account.model.User

@Service
class UserService(val users: MutableList<User>) {
    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun findUserByName(name: String): User = coroutineScope {
        val user = users.first { it.name == name }

        log.info("request-response Response: {}", user)
        user
    }

    suspend fun save(model: User) = coroutineScope {
        users.add(model)
        log.info("after calling the save method, current userList: {}", users)
    }

    suspend fun finAllUsers(): Flow<User> = flow<User> {
        repeat(4){
            val user = users.first().copy(age = it)
            log.info("find a user from the server, age: $it")
            emit(user)
        }
    }
}